package blog.service.impl;

import blog.entity.Album;
import blog.entity.Article;
import blog.entity.Chatter;
import blog.entity.ChatterImage;
import blog.entity.FriendLink;
import blog.entity.Music;
import blog.entity.Photo;
import blog.entity.Singer;
import blog.entity.User;
import blog.mapper.AlbumMapper;
import blog.mapper.ArticleMapper;
import blog.mapper.ChatterImageMapper;
import blog.mapper.ChatterMapper;
import blog.mapper.FriendLinkMapper;
import blog.mapper.MusicMapper;
import blog.mapper.PhotoMapper;
import blog.mapper.SingerMapper;
import blog.mapper.UserMapper;
import blog.service.MediaRefResolver;
import blog.vo.MediaRefVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 媒体引用解析实现
 *
 * <p>引用来源清单（新增持有文件 URL 的业务表时，在此补充；扫描与删除校验同时生效）：
 * <ul>
 *   <li>{@code t_article.cover_image} —— 封面图（纯 URL 列）</li>
 *   <li>{@code t_article.content} —— 正文内嵌图片，仅 Markdown {@code ![](url)} 语法</li>
 *   <li>{@code t_chatter_image.url} —— 说说配图（纯 URL 列）</li>
 *   <li>{@code t_photo.url} —— 相册图片（纯 URL 列）</li>
 *   <li>{@code t_music.file_url} —— 音频（纯 URL 列）</li>
 *   <li>{@code t_singer.picture_url} —— 歌手封面（纯 URL 列）</li>
 *   <li>{@code t_user.avatar} —— 用户头像（纯 URL 列）</li>
 *   <li>{@code t_friend_link.avatar} —— 友链头像（纯 URL 列）</li>
 * </ul>
 *
 * <p>不纳入：
 * <ul>
 *   <li>{@code t_project.github_url}、{@code t_bookmark.url}、{@code t_friend_link.url}
 *       —— 外部站点地址，不指向本站文件</li>
 *   <li>{@code t_chatter.content} —— 说说正文不支持 URL</li>
 *   <li>{@code t_literature.content} —— 文学作品正文，无图片</li>
 *   <li>{@code t_about.item_value} —— 关于页配置，无图片要求</li>
 * </ul>
 *
 * <p>匹配方式为等值查找：全部来源都归约为确定的 URL 键。
 * 精确列本就是纯 URL；文章正文用正则抽取出 Markdown 图片地址。
 * 上传接口写入的 {@code fileUrl} 与编辑器插入的 Markdown 地址由同一变量拼出，
 * 逐字符相同，故无需处理相对路径、query 参数或 HTML 标签。</p>
 */
@Service
public class MediaRefResolverImpl implements MediaRefResolver {

    /** 单文件被引用条目上限，防止异常数据导致返回体过大 */
    private static final int MAX_REFS_PER_FILE = 200;

    /**
     * 抽取 Markdown 图片地址。
     *
     * <p>匹配 {@code ![alt](url)} / {@code ![](url)}，地址后允许跟 title 部分。
     * 与前端 {@code MarkdownEditor.insertImage()} 写出的格式对应。</p>
     */
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\(\\s*([^)\\s]+)");

    private final ArticleMapper articleMapper;
    private final ChatterMapper chatterMapper;
    private final ChatterImageMapper chatterImageMapper;
    private final AlbumMapper albumMapper;
    private final PhotoMapper photoMapper;
    private final MusicMapper musicMapper;
    private final SingerMapper singerMapper;
    private final UserMapper userMapper;
    private final FriendLinkMapper friendLinkMapper;

    public MediaRefResolverImpl(ArticleMapper articleMapper,
                                ChatterMapper chatterMapper,
                                ChatterImageMapper chatterImageMapper,
                                AlbumMapper albumMapper,
                                PhotoMapper photoMapper,
                                MusicMapper musicMapper,
                                SingerMapper singerMapper,
                                UserMapper userMapper,
                                FriendLinkMapper friendLinkMapper) {
        this.articleMapper = articleMapper;
        this.chatterMapper = chatterMapper;
        this.chatterImageMapper = chatterImageMapper;
        this.albumMapper = albumMapper;
        this.photoMapper = photoMapper;
        this.musicMapper = musicMapper;
        this.singerMapper = singerMapper;
        this.userMapper = userMapper;
        this.friendLinkMapper = friendLinkMapper;
    }

    @Override
    public Map<String, List<MediaRefVO>> buildRefIndex() {
        Map<String, List<MediaRefVO>> index = new HashMap<>();

        // ---- 纯 URL 列，直接登记 ----

        // 文章封面 + 正文内嵌图片
        for (Article a : articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getContent, Article::getCoverImage))) {
            put(index, a.getCoverImage(), new MediaRefVO("article", a.getTitle(), a.getId(), "coverImage"));
            for (String url : extractMarkdownImages(a.getContent())) {
                put(index, url, new MediaRefVO("article", a.getTitle(), a.getId(), "content"));
            }
        }

        // 说说配图（标题取所属说说的内容摘要，便于辨认）
        Map<Long, String> chatterTitles = chatterMapper.selectList(new LambdaQueryWrapper<Chatter>()
                        .select(Chatter::getId, Chatter::getContent)).stream()
                .collect(Collectors.toMap(Chatter::getId, c -> snippet(c.getContent()), (a, b) -> a));
        for (ChatterImage ci : chatterImageMapper.selectList(new LambdaQueryWrapper<ChatterImage>()
                .select(ChatterImage::getId, ChatterImage::getUrl, ChatterImage::getChatterId))) {
            String title = ci.getChatterId() == null
                    ? "说说" : chatterTitles.getOrDefault(ci.getChatterId(), "说说");
            put(index, ci.getUrl(), new MediaRefVO("chatter", title, ci.getId(), "content"));
        }

        // 相册图片（标题取所属相册名）
        Map<Long, String> albumTitles = albumMapper.selectList(new LambdaQueryWrapper<Album>()
                        .select(Album::getId, Album::getTitle)).stream()
                .collect(Collectors.toMap(Album::getId, Album::getTitle, (a, b) -> a));
        for (Photo p : photoMapper.selectList(new LambdaQueryWrapper<Photo>()
                .select(Photo::getId, Photo::getUrl, Photo::getAlbumId))) {
            String title = p.getAlbumId() == null
                    ? "相册" : albumTitles.getOrDefault(p.getAlbumId(), "相册");
            put(index, p.getUrl(), new MediaRefVO("album", title, p.getId(), "content"));
        }

        // 音乐音频
        for (Music m : musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .select(Music::getId, Music::getTitle, Music::getFileUrl))) {
            put(index, m.getFileUrl(), new MediaRefVO("music", m.getTitle(), m.getId(), "fileUrl"));
        }

        // 歌手封面
        for (Singer s : singerMapper.selectList(new LambdaQueryWrapper<Singer>()
                .select(Singer::getId, Singer::getName, Singer::getPictureUrl))) {
            put(index, s.getPictureUrl(), new MediaRefVO("singer", s.getName(), s.getId(), "pictureUrl"));
        }

        // 用户头像
        for (User u : userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getAvatar))) {
            put(index, u.getAvatar(), new MediaRefVO("user", u.getUsername(), u.getId(), "avatar"));
        }

        // 友链头像
        for (FriendLink f : friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLink>()
                .select(FriendLink::getId, FriendLink::getName, FriendLink::getAvatar))) {
            put(index, f.getAvatar(), new MediaRefVO("friendLink", f.getName(), f.getId(), "avatar"));
        }

        return index;
    }

    /** 抽取 Markdown 正文中的全部图片地址 */
    private static List<String> extractMarkdownImages(String content) {
        if (content == null || content.isEmpty()) {
            return List.of();
        }
        List<String> urls = new ArrayList<>();
        Matcher m = MD_IMAGE.matcher(content);
        while (m.find()) {
            String url = m.group(1).strip();
            if (!url.isEmpty()) {
                urls.add(url);
            }
        }
        return urls;
    }

    private static void put(Map<String, List<MediaRefVO>> index, String url, MediaRefVO ref) {
        if (url == null || url.isBlank()) {
            return;
        }
        List<MediaRefVO> refs = index.computeIfAbsent(url, k -> new ArrayList<>());
        if (refs.size() < MAX_REFS_PER_FILE) {
            refs.add(ref);
        }
    }

    /** 长文本取前 30 字作为标题，便于管理页辨认引用来源 */
    private static String snippet(String text) {
        if (text == null || text.isBlank()) {
            return "说说";
        }
        String t = text.strip();
        return t.length() <= 30 ? t : t.substring(0, 30) + "…";
    }
}
