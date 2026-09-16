package blog.dto;

import lombok.Data;

/**
 * 播放请求
 *
 * <p>选曲与计数合并为一个接口：源项目需要 {@code /play} + {@code /next} +
 * {@code /position} 三次请求才能完成一次切歌，此处合并为一次。</p>
 */
@Data
public class PlayRequest {
    /** 当前播放的歌曲ID；为 null 表示首次播放 */
    private Long currentMusicId;

    /**
     * 播放模式：
     * <ul>
     *   <li>{@code loop} —— 顺序往后（下一首），到尾回到第一首</li>
     *   <li>{@code reverse} —— 顺序往前（上一首），到头回到最后一首</li>
     *   <li>{@code single} —— 单曲循环，返回当前歌曲本身</li>
     *   <li>{@code random} —— 同范围内随机</li>
     * </ul>
     */
    private String playMode;

    /**
     * 是否计入播放：
     * <ul>
     *   <li>{@code true} —— 自动切换（一首播完自行跳到下一首）</li>
     *   <li>{@code false} —— 手动切换（用户点击按钮）</li>
     * </ul>
     * 两种情况下 {@code last_played} 都会更新（更新给下一首），
     * 但 {@code play_count} 与累计播放时长仅在 true 时累加。
     */
    private Boolean addPlay;

    /** 分页大小，用于计算返回歌曲所在的页码 */
    private Integer pageSize;

    /** 选曲范围过滤条件 */
    private MusicQueryDTO query;
}
