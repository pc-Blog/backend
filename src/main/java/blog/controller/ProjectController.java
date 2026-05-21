package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Project;
import blog.entity.Technology;
import blog.mapper.TechnologyMapper;
import blog.service.ProjectService;
import blog.vo.ProjectDetailVO;
import blog.vo.ProjectListVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;
    private final TechnologyMapper technologyMapper;

    public ProjectController(ProjectService projectService, TechnologyMapper technologyMapper) {
        this.projectService = projectService;
        this.technologyMapper = technologyMapper;
    }

    // ==================== 访客端 ====================

    @PostMapping("/public/page")
    public Result<PageVO<ProjectListVO>> publicPage(@RequestBody PageDTO<?> dto,
                                                    @RequestParam(required = false) Long categoryId,
                                                    @RequestParam(required = false) Long techId) {
        log.info("访客端分页查询项目, categoryId:{}, techId:{}", categoryId, techId);
        return Result.success(projectService.publicPage(dto.getPageNum(), dto.getPageSize(), categoryId, techId));
    }

    @GetMapping("/public/{id}")
    public Result<ProjectDetailVO> publicDetail(@PathVariable Long id) {
        log.info("访客端查看项目详情, id:{}", id);
        return Result.success(projectService.publicDetail(id));
    }

    // ==================== 技术栈(公开) ====================

    @GetMapping("/tech/list")
    public Result<List<Technology>> techList() {
        return Result.success(technologyMapper.selectList(
                new LambdaQueryWrapper<Technology>().eq(Technology::getDeleted, 0)));
    }

    // ==================== 管理端 ====================

    @GetMapping("/{id}")
    public Result<ProjectDetailVO> getById(@PathVariable Long id) {
        log.info("根据ID查询项目, id:{}", id);
        return Result.success(projectService.adminDetail(id));
    }

    @PostMapping
    public Result<Project> save(@Valid @RequestBody Project project) {
        log.info("新增项目:{}", JSON.toJSONString(project, SerializerFeature.PrettyFormat));
        projectService.save(project);
        return Result.success(project);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Project project) {
        log.info("更新项目:{}", JSON.toJSONString(project, SerializerFeature.PrettyFormat));
        projectService.updateById(project);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除项目, id:{}", id);
        projectService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<ProjectListVO>> page(@RequestBody PageDTO<Project> dto) {
        log.info("管理端分页查询项目:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(projectService.adminPage(dto));
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布项目, id:{}", id);
        projectService.publish(id);
        return Result.success();
    }

    @PutMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("下架项目, id:{}", id);
        projectService.unpublish(id);
        return Result.success();
    }

    // ==================== 技术栈管理 ====================

    @PostMapping("/tech")
    public Result<Void> saveTech(@RequestBody Technology tech) {
        log.info("新增技术:{}", tech.getName());
        if (tech.getId() == null) {
            technologyMapper.insert(tech);
        } else {
            technologyMapper.updateById(tech);
        }
        return Result.success();
    }

    @DeleteMapping("/tech/{id}")
    public Result<Void> deleteTech(@PathVariable Long id) {
        log.info("删除技术, id:{}", id);
        technologyMapper.deleteById(id);
        return Result.success();
    }
}
