package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Photo;
import blog.service.PhotoService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/by-album/{albumId}")
    public Result<List<Photo>> getByAlbumId(@PathVariable Long albumId) {
        log.info("按相册查询照片, albumId:{}", albumId);
        return Result.success(photoService.lambdaQuery()
                .eq(Photo::getDeleted, 0)
                .eq(Photo::getAlbumId, albumId)
                .orderByAsc(Photo::getSortOrder)
                .orderByDesc(Photo::getId)
                .list());
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Photo photo) {
        log.info("新增照片:{}", JSON.toJSONString(photo, SerializerFeature.PrettyFormat));
        photoService.save(photo);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Photo photo) {
        log.info("更新照片:{}", JSON.toJSONString(photo, SerializerFeature.PrettyFormat));
        photoService.updateById(photo);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除照片, id:{}", id);
        photoService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Photo>> page(@RequestBody PageDTO<Photo> dto) {
        log.info("分页查询照片:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(photoService.page(dto));
    }
}
