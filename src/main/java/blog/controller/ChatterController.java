package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Chatter;
import blog.service.ChatterService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatter")
public class ChatterController {

    private final ChatterService chatterService;

    public ChatterController(ChatterService chatterService) {
        this.chatterService = chatterService;
    }

    @GetMapping("/list")
    public Result<List<Chatter>> getPublishedList() {
        log.info("查询已发布说说列表");
        return Result.success(chatterService.getPublishedListWithImages());
    }

    @GetMapping("/{id}")
    public Result<Chatter> getById(@PathVariable Long id) {
        log.info("根据ID查询说说, id:{}", id);
        Chatter chatter = chatterService.getById(id);
        return Result.success(chatter);
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Chatter chatter) {
        log.info("新增说说:{}", JSON.toJSONString(chatter, SerializerFeature.PrettyFormat));
        chatterService.save(chatter);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Chatter chatter) {
        log.info("更新说说:{}", JSON.toJSONString(chatter, SerializerFeature.PrettyFormat));
        chatterService.updateById(chatter);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除说说, id:{}", id);
        chatterService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Chatter>> page(@RequestBody PageDTO<Chatter> dto) {
        log.info("分页查询说说:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(chatterService.page(dto));
    }
}
