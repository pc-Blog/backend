package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐展示对象
 *
 * <p>在 {@code t_music} 基础上补充歌手名与分类名，避免前端二次查询。
 * 不含 {@code t_media} 的字段：文件信息由 {@code file_url} 直接访问。</p>
 */
@Data
public class MusicVO {
    private Long id;
    private String title;
    private String fileUrl;
    private Integer duration;
    private Integer playCount;
    private Boolean isFavorite;
    private LocalDateTime lastPlayed;
    private Long singerId;
    private String singerName;
    /** 歌手封面URL，为空时前端回落默认图 */
    private String singerPictureUrl;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
