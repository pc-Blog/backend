package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Singer;
import blog.exception.BaseException;
import blog.service.SingerService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/singer")
public class SingerController {

    private final SingerService singerService;

    public SingerController(SingerService singerService) {
        this.singerService = singerService;
    }

    @PostMapping("/page")
    public Result<PageVO<Singer>> page(@RequestBody PageDTO<Singer> dto) {
        log.info("分页查询歌手:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(singerService.page(dto));
    }

    @GetMapping("/{id}")
    public Result<Singer> getById(@PathVariable Long id) {
        log.info("根据ID查询歌手, id:{}", id);
        Singer singer = singerService.getById(id);
        if (singer == null) {
            throw new BaseException("歌手不存在");
        }
        return Result.success(singer);
    }

    @PostMapping
    public Result<Singer> save(@Valid @RequestBody Singer singer) {
        log.info("新增歌手:{}", JSON.toJSONString(singer, SerializerFeature.PrettyFormat));
        singerService.save(singer);
        return Result.success(singer);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Singer singer) {
        log.info("更新歌手:{}", JSON.toJSONString(singer, SerializerFeature.PrettyFormat));
        singerService.updateById(singer);
        return Result.success();
    }

    /** 逻辑删除；不检查引用、不级联，挂在其下的歌曲保留 singer_id */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除歌手, id:{}", id);
        singerService.removeById(id);
        return Result.success();
    }
}
