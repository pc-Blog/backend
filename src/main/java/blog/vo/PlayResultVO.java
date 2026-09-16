package blog.vo;

import lombok.Data;

/**
 * 播放结果
 */
@Data
public class PlayResultVO {
    /** 下一首待播放的歌曲 */
    private MusicVO nextMusic;

    /** 该歌曲在当前过滤条件下的页码（从 1 开始） */
    private Integer position;
}
