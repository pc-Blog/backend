package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.MusicCategory;
import blog.exception.BaseException;
import blog.service.MusicCategoryService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/music-category")
public class MusicCategoryController {

    private final MusicCategoryService musicCategoryService;

    public MusicCategoryController(MusicCategoryService musicCategoryService) {
        this.musicCategoryService = musicCategoryService;
    }

    @PostMapping("/page")
    public Result<PageVO<MusicCategory>> page(@RequestBody PageDTO<MusicCategory> dto) {
        log.info("分页查询音乐分类:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(musicCategoryService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<MusicCategory> getById(@PathVariable Long id) {
        log.info("根据ID查询音乐分类, id:{}", id);
        MusicCategory category = musicCategoryService.getById(id);
        if (category == null) {
            throw new BaseException("分类不存在");
        }
        return Result.success(category);
    }

    @PostMapping
    public Result<MusicCategory> save(@Valid @RequestBody MusicCategory category) {
        log.info("新增音乐分类:{}", JSON.toJSONString(category, SerializerFeature.PrettyFormat));
        musicCategoryService.save(category);
        return Result.success(category);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody MusicCategory category) {
        log.info("更新音乐分类:{}", JSON.toJSONString(category, SerializerFeature.PrettyFormat));
        musicCategoryService.updateById(category);
        return Result.success();
    }

    /** 逻辑删除；不检查引用、不级联，挂在其下的歌曲保留 category_id */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除音乐分类, id:{}", id);
        musicCategoryService.removeById(id);
        return Result.success();
    }
}
