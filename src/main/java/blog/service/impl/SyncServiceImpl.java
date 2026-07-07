package blog.service.impl;

import blog.common.Result;
import blog.entity.*;
import blog.mapper.*;
import blog.service.SyncService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class SyncServiceImpl implements SyncService {

    @Value("${worker.api-url}")
    private String workerApiUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();

    private final ArticleMapper articleMapper;
    private final EmailMapper emailMapper;
    private final SubscriberMapper subscriberMapper;
    private final CommentReactionMapper commentReactionMapper;
    private final CommentUpvoteMapper commentUpvoteMapper;
    private final PushLogMapper pushLogMapper;
    private final UserMapper userMapper;

    private static final List<String> TABLE_ORDER = List.of(
            "views", "emails", "subscribers", "reactions", "upvotes", "push-logs", "users"
    );

    public SyncServiceImpl(ArticleMapper articleMapper,
                           EmailMapper emailMapper,
                           SubscriberMapper subscriberMapper,
                           CommentReactionMapper commentReactionMapper,
                           CommentUpvoteMapper commentUpvoteMapper,
                           PushLogMapper pushLogMapper,
                           UserMapper userMapper) {
        this.articleMapper = articleMapper;
        this.emailMapper = emailMapper;
        this.subscriberMapper = subscriberMapper;
        this.commentReactionMapper = commentReactionMapper;
        this.commentUpvoteMapper = commentUpvoteMapper;
        this.pushLogMapper = pushLogMapper;
        this.userMapper = userMapper;
    }

    // ════════════════════════════════════════════
    // 接口
    // ════════════════════════════════════════════

    @Override
    public Result<Map<String, Object>> syncAll(boolean overwrite) {
        Map<String, Object> results = new LinkedHashMap<>();
        boolean allOk = true;
        for (String name : TABLE_ORDER) {
            try {
                results.put(name, syncTable(name, overwrite));
            } catch (Exception e) {
                log.error("同步 {} 失败", name, e);
                results.put(name, Map.of("error", e.getMessage()));
                allOk = false;
            }
        }
        return allOk ? Result.success(results) : Result.error("部分同步失败");
    }

    @Override
    public Result<Map<String, Object>> syncTable(String tableName, boolean overwrite) {
        return switch (tableName) {
            case "views" -> doSyncViews();
            case "emails" -> doSyncEmails(overwrite);
            case "subscribers" -> doSyncSubscribers(overwrite);
            case "reactions" -> doSyncReactions(overwrite);
            case "upvotes" -> doSyncUpvotes(overwrite);
            case "push-logs" -> doSyncPushLogs(overwrite);
            case "users" -> doSyncUsers(overwrite);
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
        JSONArray rows = fetchWorker("views", null);
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

    private Result<Map<String, Object>> doSyncEmails(boolean overwrite) {
        if (overwrite) emailMapper.delete(null);
        JSONArray rows = fetchWorker("emails", overwrite ? null : getLastSyncStr("emails"));
        if (rows == null) return Result.error("拉取 emails 失败");
        int saved = batchInsert(rows, row -> {
            Email e = new Email();
            e.setMessageId(row.getString("message_id"));
            e.setFromAddr(row.getString("from_addr"));
            e.setToAddr(row.getString("to_addr"));
            e.setForwardTo(row.getString("forward_to"));
            e.setSubject(row.getString("subject"));
            e.setTextBody(row.getString("text_body"));
            e.setHtmlBody(row.getString("html_body"));
            e.setHeaders(row.getString("headers"));
            e.setCreatedAt(parseTime(row.getString("created_at")));
            emailMapper.insert(e);
        });
        return result(rows.size(), saved, "emails");
    }

    private Result<Map<String, Object>> doSyncSubscribers(boolean overwrite) {
        if (overwrite) subscriberMapper.delete(null);
        JSONArray rows = fetchWorker("subscribers", overwrite ? null : getLastSyncStr("subscribers"));
        if (rows == null) return Result.error("拉取 subscribers 失败");
        int saved = batchInsert(rows, row -> {
            Subscriber s = new Subscriber();
            s.setEmail(row.getString("email"));
            s.setGroupName(row.getString("group_name"));
            s.setCreatedAt(parseTime(row.getString("created_at")));
            subscriberMapper.insert(s);
        });
        return result(rows.size(), saved, "subscribers");
    }

    private Result<Map<String, Object>> doSyncReactions(boolean overwrite) {
        if (overwrite) commentReactionMapper.delete(null);
        JSONArray rows = fetchWorker("reactions", overwrite ? null : getLastSyncStr("reactions"));
        if (rows == null) return Result.error("拉取 reactions 失败");
        int saved = batchInsert(rows, row -> {
            CommentReaction r = new CommentReaction();
            r.setSubjectId(row.getString("subject_id"));
            r.setUserId(row.getLong("user_id"));
            r.setReaction(row.getString("reaction"));
            r.setCreatedAt(parseTime(row.getString("created_at")));
            commentReactionMapper.insert(r);
        });
        return result(rows.size(), saved, "reactions");
    }

    private Result<Map<String, Object>> doSyncUpvotes(boolean overwrite) {
        if (overwrite) commentUpvoteMapper.delete(null);
        JSONArray rows = fetchWorker("upvotes", overwrite ? null : getLastSyncStr("upvotes"));
        if (rows == null) return Result.error("拉取 upvotes 失败");
        int saved = batchInsert(rows, row -> {
            CommentUpvote u = new CommentUpvote();
            u.setSubjectId(row.getString("subject_id"));
            u.setUserId(row.getLong("user_id"));
            u.setCreatedAt(parseTime(row.getString("created_at")));
            commentUpvoteMapper.insert(u);
        });
        return result(rows.size(), saved, "upvotes");
    }

    private Result<Map<String, Object>> doSyncPushLogs(boolean overwrite) {
        if (overwrite) pushLogMapper.delete(null);
        JSONArray rows = fetchWorker("push-logs", overwrite ? null : getLastSyncStr("push-logs"));
        if (rows == null) return Result.error("拉取 push-logs 失败");
        int saved = batchInsert(rows, row -> {
            PushLog p = new PushLog();
            p.setPushedAt(parseTime(row.getString("pushed_at")));
            p.setArticleCount(row.getInteger("article_count"));
            p.setSubscriberCount(row.getInteger("subscriber_count"));
            p.setGroupName(row.getString("group_name"));
            p.setStatus(row.getString("status"));
            p.setErrorMsg(row.getString("error_msg"));
            p.setArticleIds(row.getString("article_ids") != null ? row.getString("article_ids") : "");
            pushLogMapper.insert(p);
        });
        return result(rows.size(), saved, "push-logs");
    }

    private Result<Map<String, Object>> doSyncUsers(boolean overwrite) {
        if (overwrite) userMapper.deleteAllPhysically();
        JSONArray rows = fetchWorker("users", overwrite ? null : getLastSyncStr("users"));
        if (rows == null) return Result.error("拉取 users 失败");
        int saved = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                JSONObject row = rows.getJSONObject(i);
                String username = row.getString("username");
                if (username == null) continue;
                if (!overwrite && userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) != null) continue;
                User u = new User();
                u.setUsername(username);
                u.setPassword(row.getString("password"));
                u.setNickname(row.getString("nickname"));
                u.setAvatar(row.getString("avatar"));
                u.setGithubId(row.getString("github_id"));
                u.setDeleted(row.getInteger("deleted"));
                u.setGithubToken(row.getString("github_token"));
                u.setGithubRefreshToken(row.getString("github_refresh_token"));
                u.setGithubTokenExpiresAt(row.getString("github_token_expires_at"));
                u.setCreateTime(parseTime(row.getString("create_time")));
                u.setUpdateTime(parseTime(row.getString("update_time")));
                userMapper.insert(u);
                saved++;
            } catch (Exception ex) {
                log.warn("用户跳过: {}", ex.getMessage());
            }
        }
        return result(rows.size(), saved, "users");
    }

    // ════════════════════════════════════════════
    // 工具
    // ════════════════════════════════════════════

    private JSONArray fetchWorker(String endpoint, String since) {
        try {
            String url = workerApiUrl + "/api/sync/" + endpoint;
            if (since != null) url += "?since=" + URLEncoder.encode(since, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;
            JSONObject json = JSON.parseObject(resp.body());
            return json.getIntValue("code") == 1 ? json.getJSONArray("data") : null;
        } catch (Exception e) {
            log.error("请求 Worker 失败: {}", endpoint, e);
            return null;
        }
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

    private String getLastSyncStr(String table) {
        LocalDateTime t = getLastSync(table);
        // 加 1 秒，避免 Worker 端 >= 查询重复拉取最后一条
        return t != null ? t.plusSeconds(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    /** 用数据本身的创建时间字段查 MAX，作为增量同步的起点 */
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

    private LocalDateTime parseTime(String s) {
        if (s == null || s.isEmpty()) return LocalDateTime.now();
        try { return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
        catch (Exception e) { try { return LocalDateTime.parse(s); } catch (Exception e2) { return LocalDateTime.now(); } }
    }
}
