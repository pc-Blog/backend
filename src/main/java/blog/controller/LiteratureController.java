package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.LiteratureQueryDTO;
import blog.entity.Category;
import blog.entity.Literature;
import blog.service.LiteratureService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/literature")
public class LiteratureController {

    private final LiteratureService literatureService;

    public LiteratureController(LiteratureService literatureService) {
        this.literatureService = literatureService;
    }

    // ==================== 访客端 ====================

    /**
     * 全部已发布作品（含完整正文）。
     *
     * <p>返回平铺列表，已按「分类 sort_order 升序 + 写作日期降序」预排。
     * 前端按分类分组时直接保持顺序即可；按年月展示时自行按 writtenAt 重排再分组。
     * 分类名请用 {@link #publicCategories()} 的返回值自行映射。</p>
     */
    @PostMapping("/public/list")
    public Result<PageVO<Literature>> publicList() {
        List<Literature> rows = literatureService.publicList();
        return Result.success(new PageVO<>(rows.size(), rows));
    }

    /** 已发布作品总数 */
    @GetMapping("/public/count")
    public Result<Long> publicCount() {
        return Result.success(literatureService.publicCount());
    }

    /** 文学分类下拉选项，前端用于映射 categoryId -> 分类名 */
    @GetMapping("/public/categories")
    public Result<List<Category>> publicCategories() {
        return Result.success(literatureService.categoryOptions());
    }

    /** 单篇详情（仅已发布） */
    @GetMapping("/public/{id}")
    public Result<Literature> publicDetail(@PathVariable Long id) {
        log.info("访客端查看作品详情, id:{}", id);
        return Result.success(literatureService.publicDetail(id));
    }

    // ==================== 管理端 ====================

    @PostMapping("/admin/page")
    public Result<PageVO<Literature>> page(@RequestBody PageDTO<LiteratureQueryDTO> dto) {
        log.info("管理端分页查询作品:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(literatureService.adminPage(dto));
    }

    @GetMapping("/admin/{id}")
    public Result<Literature> getById(@PathVariable Long id) {
        log.info("根据ID查询作品, id:{}", id);
        return Result.success(literatureService.adminDetail(id));
    }

    @PostMapping("/admin")
    public Result<Literature> save(@Valid @RequestBody Literature literature) {
        log.info("新增作品:{}", JSON.toJSONString(literature, SerializerFeature.PrettyFormat));
        literatureService.checkCategoryValid(literature.getCategoryId());
        literatureService.save(literature);
        return Result.success(literature);
    }

    @PutMapping("/admin")
    public Result<Void> update(@Valid @RequestBody Literature literature) {
        log.info("更新作品:{}", JSON.toJSONString(literature, SerializerFeature.PrettyFormat));
        literatureService.checkCategoryValid(literature.getCategoryId());
        literatureService.update(literature);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除作品, id:{}", id);
        literatureService.delete(id);
        return Result.success();
    }

    @PutMapping("/admin/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布作品, id:{}", id);
        literatureService.publish(id);
        return Result.success();
    }

    @PutMapping("/admin/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("下架作品, id:{}", id);
        literatureService.unpublish(id);
        return Result.success();
    }
}
