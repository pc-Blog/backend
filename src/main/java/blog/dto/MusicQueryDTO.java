package blog.dto;

import lombok.Data;

/**
 * 音乐查询条件
 *
 * <p>用于分页查询与播放接口的选曲范围过滤。</p>
 */
@Data
public class MusicQueryDTO {
    /** 标题模糊匹配 */
    private String title;

    /** 歌手ID（NULL 表示不过滤） */
    private Long singerId;

    /** 分类ID（NULL 表示不过滤） */
    private Long categoryId;

    /** 仅收藏：true 时只返回收藏曲目 */
    private Boolean onlyFavorite;
}
