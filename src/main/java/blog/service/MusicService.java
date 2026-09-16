package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.MusicQueryDTO;
import blog.dto.PlayRequest;
import blog.entity.Music;
import blog.vo.MusicVO;
import blog.vo.PlayResultVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 音乐服务
 *
 * <p>音频本体登记在 {@code t_media}，本模块只维护 {@code t_music} 与文件 URL 的引用关系。
 * 三张表均为逻辑删除；删歌不级联到文件，文件清理由前端孤儿扫描负责。</p>
 */
public interface MusicService extends IService<Music> {

    /** 分页查询（含歌手名与分类名） */
    PageVO<MusicVO> page(PageDTO<MusicQueryDTO> dto);

    /** 单曲详情 */
    MusicVO detail(Long id);

    /**
     * 批量上传音频。
     *
     * <p>每个文件先经 {@code MediaService.upload} 登记到 {@code t_media}
     * （relation_type='music'），再以返回的 fileUrl 写入 {@code t_music}。</p>
     *
     * @return 新增歌曲的 ID 列表，顺序与入参一致
     */
    List<Long> batchUpload(MultipartFile[] files, Long singerId, Long categoryId);

    /** 更新歌曲业务信息（标题 / 歌手 / 分类 / 收藏） */
    void updateMusic(Music music);

    /** 逻辑删除（只置 deleted=1，不动 t_media、不动 MinIO 文件） */
    void deleteMusic(Long id);

    /** 切换收藏状态，返回切换后的值 */
    boolean toggleFavorite(Long id);

    /**
     * 播放：按模式选下一首，并按 {@code addPlay} 决定是否计数。
     *
     * <p>无论手动还是自动切换，都会把下一首的 {@code last_played} 更新为当前时间；
     * {@code play_count} 与累计播放时长仅在自动切换（{@code addPlay=true}）时累加。</p>
     */
    PlayResultVO play(PlayRequest request);

    /** 读取累计播放时长（秒），即 {@code t_about} 中 is_system=1 的 music_play_duration */
    long getTotalPlayDuration();
}
