package blog.controller;

import blog.common.Result;
import blog.dto.DiaryRequestDTO;
import blog.service.DiaryService;
import blog.vo.DiaryStatsVO;
import blog.vo.DiaryVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日记
 *
 * <p>仅本地使用，不参与静态数据同步，因此没有 public 接口，也没有发布状态。
 * 全量返回，不分页：存量数百条，年/月筛选与分组在前端内存中完成。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /** 全量日记（按日期降序），活动条目内嵌 */
    @GetMapping
    public Result<List<DiaryVO>> list() {
        log.info("查询全部日记");
        return Result.success(diaryService.listAll());
    }

    /** 统计：总数 / 每日条目数 / 分类分布 / 小分类分布 */
    @GetMapping("/stats")
    public Result<DiaryStatsVO> stats() {
        log.info("查询日记统计");
        return Result.success(diaryService.stats());
    }

    /** 指定分类下已使用过的小分类名称 */
    @GetMapping("/subcategories")
    public Result<List<String>> subcategories(@RequestParam Integer category) {
        return Result.success(diaryService.subcategories(category));
    }

    /** 单天详情 */
    @GetMapping("/{id}")
    public Result<DiaryVO> getById(@PathVariable Long id) {
        log.info("查询日记, id:{}", id);
        return Result.success(diaryService.get(id));
    }

    /** 新建一天 */
    @PostMapping
    public Result<Long> save(@Valid @RequestBody DiaryRequestDTO request) {
        log.info("新增日记:{}", JSON.toJSONString(request, SerializerFeature.PrettyFormat));
        return Result.success(diaryService.create(request));
    }

    /** 更新一天（活动整体替换，日期不可修改） */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DiaryRequestDTO request) {
        log.info("更新日记:{}", JSON.toJSONString(request, SerializerFeature.PrettyFormat));
        diaryService.update(request);
        return Result.success();
    }

    /** 删除一天及其全部活动条目 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除日记, id:{}", id);
        diaryService.delete(id);
        return Result.success();
    }
}
