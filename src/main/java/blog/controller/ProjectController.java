package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Project;
import blog.service.ProjectService;
import blog.vo.ProjectListVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ==================== 访客端 ====================

    @PostMapping("/public/page")
    public Result<PageVO<ProjectListVO>> publicPage(@RequestBody PageDTO<?> dto) {
        log.info("访客端分页查询项目卡片");
        return Result.success(projectService.publicPage(dto.getPageNum(), dto.getPageSize()));
    }

    // ==================== 管理端 ====================

    @GetMapping("/{id}")
    public Result<ProjectListVO> getById(@PathVariable Long id) {
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
}