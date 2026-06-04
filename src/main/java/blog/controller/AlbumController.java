package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Album;
import blog.service.AlbumService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/album")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/list")
    public Result<List<Album>> getPublishedList() {
        log.info("查询已发布相册列表");
        return Result.success(albumService.lambdaQuery()
                .eq(Album::getDeleted, 0)
                .eq(Album::getIsPublished, 1)
                .orderByDesc(Album::getSortOrder)
                .orderByDesc(Album::getId)
                .list());
    }

    @GetMapping("/{id}")
    public Result<Album> getById(@PathVariable Long id) {
        log.info("根据ID查询相册, id:{}", id);
        Album album = albumService.getById(id);
        return Result.success(album);
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Album album) {
        log.info("新增相册:{}", JSON.toJSONString(album, SerializerFeature.PrettyFormat));
        albumService.save(album);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Album album) {
        log.info("更新相册:{}", JSON.toJSONString(album, SerializerFeature.PrettyFormat));
        albumService.updateById(album);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除相册, id:{}", id);
        albumService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Album>> page(@RequestBody PageDTO<Album> dto) {
        log.info("分页查询相册:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(albumService.page(dto));
    }
}
