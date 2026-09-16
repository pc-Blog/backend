package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.MusicQueryDTO;
import blog.dto.PlayRequest;
import blog.entity.Music;
import blog.service.MusicService;
import blog.vo.MusicVO;
import blog.vo.PlayResultVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    // ==================== 查询 ====================

    @PostMapping("/page")
    public Result<PageVO<MusicVO>> page(@RequestBody PageDTO<MusicQueryDTO> dto) {
        log.info("分页查询音乐:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(musicService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<MusicVO> getById(@PathVariable Long id) {
        log.info("根据ID查询音乐, id:{}", id);
        return Result.success(musicService.detail(id));
    }

    /** 累计播放时长（秒），用于展示「听歌时间」 */
    @GetMapping("/play-duration")
    public Result<Long> playDuration() {
        return Result.success(musicService.getTotalPlayDuration());
    }

    // ==================== 增删改 ====================

    /**
     * 批量上传音频。
     *
     * <p>每个文件经 {@code /api/media/upload} 同一套逻辑登记到 {@code t_media}，
     * 因此统一落 {@code yyyyMM/{uuid}{ext}} 路径。</p>
     */
    @PostMapping("/batch")
    public Result<List<Long>> batchUpload(@RequestParam("files") MultipartFile[] files,
                                          @RequestParam(value = "singerId", required = false) Long singerId,
                                          @RequestParam(value = "categoryId", required = false) Long categoryId) {
        log.info("批量上传音乐: 文件数={}, singerId={}, categoryId={}", files.length, singerId, categoryId);
        return Result.success(musicService.batchUpload(files, singerId, categoryId));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Music music) {
        log.info("更新音乐:{}", JSON.toJSONString(music, SerializerFeature.PrettyFormat));
        musicService.updateMusic(music);
        return Result.success();
    }

    /** 逻辑删除；不动 t_media、不动 MinIO 文件（文件由前端孤儿扫描清理） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除音乐, id:{}", id);
        musicService.deleteMusic(id);
        return Result.success();
    }

    // ==================== 播放 ====================

    @PostMapping("/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        log.info("切换收藏状态, id:{}", id);
        return Result.success(musicService.toggleFavorite(id));
    }

    /**
     * 播放：选下一首并按需计数。
     *
     * <p>合并了源项目的 {@code /play}、{@code /next}、{@code /position} 三个接口：
     * 一次请求即返回下一首歌曲及其页码，同时按 {@code addPlay} 决定是否累加播放量。</p>
     */
    @PostMapping("/play")
    public Result<PlayResultVO> play(@RequestBody PlayRequest request) {
        log.info("播放:{}", JSON.toJSONString(request, SerializerFeature.PrettyFormat));
        return Result.success(musicService.play(request));
    }
}
