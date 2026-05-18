package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Timeline;
import blog.service.TimelineService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/list")
    public Result<List<Timeline>> list() {
        log.info("访客端查询时间线列表");
        List<Timeline> list = timelineService.list(
                new LambdaQueryWrapper<Timeline>()
                        .eq(Timeline::getDeleted, 0)
                        .orderByDesc(Timeline::getEventDate));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Timeline> getById(@PathVariable Long id) {
        log.info("根据ID查询时间线, id:{}", id);
        return Result.success(timelineService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody Timeline timeline) {
        log.info("新增时间线:{}", JSON.toJSONString(timeline, SerializerFeature.PrettyFormat));
        timelineService.save(timeline);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Timeline timeline) {
        log.info("更新时间线:{}", JSON.toJSONString(timeline, SerializerFeature.PrettyFormat));
        timelineService.updateById(timeline);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除时间线, id:{}", id);
        timelineService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Timeline>> page(@RequestBody PageDTO<Timeline> dto) {
        log.info("分页查询时间线:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(timelineService.page(dto));
    }
}
