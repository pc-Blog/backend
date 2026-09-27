package blog.service.impl;

import blog.common.Result;
import blog.entity.*;
import blog.mapper.*;
import blog.service.SyncService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * D1 数据同步服务：把 Worker 的 D1 表整表拉回来覆盖 PostgreSQL 镜像。
 *
 * <p>只有全量覆盖一种方式——每张表先清空再按 D1 原样导入，没有增量模式，
 * 因此不需要游标，也不需要区分新增与更新。
 *
 * <p>无状态、线程安全。依赖 {@code worker.api-url} 与 {@code worker.admin-token} 两项配置，
 * 两者缺失时拉取会失败并返回错误，不会清空镜像表。
 */
@Slf4j
@Service
public class SyncServiceImpl implements SyncService {

    @Value("${worker.api-url}")
    private String workerApiUrl;

    @Value("${worker.admin-token}")
    private String workerAdminToken;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();

    private final ArticleMapper articleMapper;
    private final EmailMapper emailMapper;
    private final SubscriberMapper subscriberMapper;
    private final CommentMapper commentMapper;
    private final CommentReactionMapper commentReactionMapper;
    private final CommentUpvoteMapper commentUpvoteMapper;
    private final PushLogMapper pushLogMapper;
    private final UserMapper userMapper;

    /** 同步顺序：users 必须早于 comments，否则评论的 user_id 找不到对应作者 */
    private static final List<String> TABLE_ORDER = List.of(
            "users", "views", "emails", "subscribers", "comments", "reactions", "upvotes", "push-logs"
    );

    public SyncServiceImpl(ArticleMapper articleMapper,
                           EmailMapper emailMapper,
                           SubscriberMapper subscriberMapper,
                           CommentMapper commentMapper,
                           CommentReactionMapper commentReactionMapper,
                           CommentUpvoteMapper commentUpvoteMapper,
                           PushLogMapper pushLogMapper,
                           UserMapper userMapper) {
        this.articleMapper = articleMapper;
        this.emailMapper = emailMapper;
        this.subscriberMapper = subscriberMapper;
        this.commentMapper = commentMapper;
        this.commentReactionMapper = commentReactionMapper;
        this.commentUpvoteMapper = commentUpvoteMapper;
        this.pushLogMapper = pushLogMapper;
        this.userMapper = userMapper;
    }

    // ════════════════════════════════════════════
    // 接口
    // ════════════════════════════════════════════

    @Override
    public Result<Map<String, Object>> syncAll() {
        Map<String, Object> results = new LinkedHashMap<>();
        List<String> failed = new ArrayList<>();
        for (String name : TABLE_ORDER) {
            try {
                results.put(name, syncTable(name));
            } catch (Exception e) {
                log.error("同步 {} 失败", name, e);
                // 用 String.valueOf 兜住 null，Map.of 不接受 null 值
                results.put(name, Map.of("error", String.valueOf(e.getMessage())));
                failed.add(name);
            }
        }
        // 失败时带上表名，否则前端只看到"部分同步失败"，无从判断是哪张表
        return failed.isEmpty()
                ? Result.success(results)
                : Result.error("部分同步失败: " + String.join(", ", failed));
    }

    @Override
    public Result<Map<String, Object>> syncTable(String tableName) {
        return switch (tableName) {
            case "views" -> doSyncViews();
            case "emails" -> doSyncEmails();
            case "subscribers" -> doSyncSubscribers();
            case "comments" -> doSyncComments();
            case "reactions" -> doSyncReactions();
            case "upvotes" -> doSyncUpvotes();
            case "push-logs" -> doSyncPushLogs();
            case "users" -> doSyncUsers();
            default -> Result.error("未知表: " + tableName);
        };
    }

    @Override
    public Result<Map<String, LocalDateTime>> syncStatus() {
        Map<String, LocalDateTime> status = new LinkedHashMap<>();
        for (String name : TABLE_ORDER) {
            status.put(name, getLastSync(name));
        }
        return Result.success(status);
    }

    // ════════════════════════════════════════════
    // 各表同步
    // ════════════════════════════════════════════

    private Result<Map<String, Object>> doSyncViews() {
        JSONArray rows = fetchWorker("views");
        if (rows == null) return Result.error("拉取 views 失败");
        int saved = 0;
        for (int i = 0; i < rows.size(); i++) {
            JSONObject row = rows.getJSONObject(i);
            Long articleId = row.getLong("article_id");
            Integer views = row.getInteger("views");
            if (articleId != null) {
                articleMapper.syncViewCount(articleId, views != null ? views : 0);
                saved++;
            }
        }
        return result(rows.size(), saved, "views");
    }

    private Result<Map<String, Object>> doSyncEmails() {
        JSONArray rows = fetchWorker("emails");
        if (rows == null) return Result.error("拉取 emails 失败");
        clear(emailMapper);
        int saved = batchInsert(rows, row -> emailMapper.insert(toEntity(row, Email.class)));
        return result(rows.size(), saved, "emails");
    }

    private Result<Map<String, Object>> doSyncSubscribers() {
        JSONArray rows = fetchWorker("subscribers");
        if (rows == null) return Result.error("拉取 subscribers 失败");
        clear(subscriberMapper);
        int saved = batchInsert(rows, row -> subscriberMapper.insert(toEntity(row, Subscriber.class)));
        return result(rows.size(), saved, "subscribers");
    }

    private Result<Map<String, Object>> doSyncComments() {
        JSONArray rows = fetchWorker("comments");
        if (rows == null) return Result.error("拉取 comments 失败");
        clear(commentMapper);
        int saved = batchInsert(rows, row -> commentMapper.insert(toEntity(row, Comment.class)));
        return result(rows.size(), saved, "comments");
    }

    private Result<Map<String, Object>> doSyncReactions() {
        JSONArray rows = fetchWorker("reactions");
        if (rows == null) return Result.error("拉取 reactions 失败");
        clear(commentReactionMapper);
        int saved = batchInsert(rows, row -> commentReactionMapper.insert(toEntity(row, CommentReaction.class)));
        return result(rows.size(), saved, "reactions");
    }

    private Result<Map<String, Object>> doSyncUpvotes() {
        JSONArray rows = fetchWorker("upvotes");
        if (rows == null) return Result.error("拉取 upvotes 失败");
        clear(commentUpvoteMapper);
        int saved = batchInsert(rows, row -> commentUpvoteMapper.insert(toEntity(row, CommentUpvote.class)));
        return result(rows.size(), saved, "upvotes");
    }

    private Result<Map<String, Object>> doSyncPushLogs() {
        JSONArray rows = fetchWorker("push-logs");
        if (rows == null) return Result.error("拉取 push-logs 失败");
        clear(pushLogMapper);
        int saved = batchInsert(rows, row -> pushLogMapper.insert(toEntity(row, PushLog.class)));
        return result(rows.size(), saved, "push-logs");
    }

    private Result<Map<String, Object>> doSyncUsers() {
        JSONArray rows = fetchWorker("users");
        if (rows == null) return Result.error("拉取 users 失败");
        clear(userMapper);
        int saved = batchInsert(rows, row -> userMapper.insert(toEntity(row, User.class)));
        return result(rows.size(), saved, "users");
    }

    // ════════════════════════════════════════════
    // 工具
    // ════════════════════════════════════════════

    /**
     * 拉取 Worker 上一张表的全量数据。
     *
     * @param endpoint {@code /api/sync/} 下的表名，如 {@code emails}
     * @return D1 的行数组；HTTP 非 200 或返回码不为 1 时返回 {@code null}
     */
    private JSONArray fetchWorker(String endpoint) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(workerApiUrl + "/api/sync/" + endpoint)).timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + workerAdminToken)
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;
            JSONObject json = JSON.parseObject(resp.body());
            return json.getIntValue("code") == 1 ? json.getJSONArray("data") : null;
        } catch (Exception e) {
            log.error("请求 Worker 失败: {}", endpoint, e);
            return null;
        }
    }

    /**
     * 清空一张镜像表，是全量覆盖的前置动作。
     *
     * <p>条件必须写成 {@code id IS NOT NULL} 而不能留空：Druid 的 WallFilter 会拒绝不带 where
     * 的 DELETE（delete none condition not allow），留空则整表删除语句根本发不出去。
     * 实体未参与全局逻辑删除，这里是物理删除，随后可以按 D1 的原 id 重新插入。
     *
     * @param mapper 目标表的 Mapper
     * @param <T>    实体类型
     */
    private <T> void clear(BaseMapper<T> mapper) {
        mapper.delete(new QueryWrapper<T>().isNotNull("id"));
    }

    /**
     * 把 D1 的一行按字段名自动映射成实体。
     *
     * <p>下划线列名（{@code from_addr}）由 fastjson 的智能匹配对应到驼峰属性，
     * 时间列解析成的 {@link LocalDateTime} 就是 D1 里的 UTC 墙钟，不做时区换算；
     * 只有列名与属性名对不上的字段（如 {@code deleted}）需要在实体上加
     * {@link com.alibaba.fastjson.annotation.JSONField} 指明。
     *
     * @param row  D1 返回的一行
     * @param type 目标实体类型
     * @param <T>  实体类型
     * @return 映射后的实体
     */
    private <T> T toEntity(JSONObject row, Class<T> type) {
        return JSON.parseObject(row.toJSONString(), type);
    }

    @FunctionalInterface
    private interface InsertFn { void run(JSONObject row) throws Exception; }

    private int batchInsert(JSONArray rows, InsertFn fn) {
        int count = 0;
        for (int i = 0; i < rows.size(); i++) {
            try { fn.run(rows.getJSONObject(i)); count++; }
            catch (Exception ex) { log.warn("跳过: {}", ex.getMessage()); }
        }
        return count;
    }

    private Result<Map<String, Object>> result(int fetched, int saved, String table) {
        Map<String, Object> m = new HashMap<>();
        m.put("table", table); m.put("fetched", fetched); m.put("saved", saved);
        return Result.success(m);
    }

    /** 取表中最新一条记录的时间，状态页据此显示该表的数据更新到什么时候 */
    private LocalDateTime getLastSync(String table) {
        return switch (table) {
            case "emails" -> {
                var last = emailMapper.selectOne(new LambdaQueryWrapper<Email>().orderByDesc(Email::getCreatedAt).last("LIMIT 1"));
                yield last != null ? last.getCreatedAt() : null;
            }
            case "subscribers" -> {
                var last = subscriberMapper.selectOne(new LambdaQueryWrapper<Subscriber>().orderByDesc(Subscriber::getCreatedAt).last("LIMIT 1"));
                yield last != null ? last.getCreatedAt() : null;
            }
            case "comments" -> {
                var last = commentMapper.selectOne(new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime).last("LIMIT 1"));
                yield last != null ? last.getCreateTime() : null;
            }
            case "reactions" -> {
                var last = commentReactionMapper.selectOne(new LambdaQueryWrapper<CommentReaction>().orderByDesc(CommentReaction::getCreatedAt).last("LIMIT 1"));
                yield last != null ? last.getCreatedAt() : null;
            }
            case "upvotes" -> {
                var last = commentUpvoteMapper.selectOne(new LambdaQueryWrapper<CommentUpvote>().orderByDesc(CommentUpvote::getCreatedAt).last("LIMIT 1"));
                yield last != null ? last.getCreatedAt() : null;
            }
            case "push-logs" -> {
                var last = pushLogMapper.selectOne(new LambdaQueryWrapper<PushLog>().orderByDesc(PushLog::getPushedAt).last("LIMIT 1"));
                yield last != null ? last.getPushedAt() : null;
            }
            case "users" -> {
                var last = userMapper.selectOne(new LambdaQueryWrapper<User>().orderByDesc(User::getUpdateTime).last("LIMIT 1"));
                yield last != null ? last.getUpdateTime() : null;
            }
            default -> null;
        };
    }
}
