package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Project;
import blog.exception.BaseException;
import blog.mapper.ProjectMapper;
import blog.service.ProjectService;
import blog.util.PageUtil;
import blog.vo.ProjectListVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    @Transactional
    public boolean save(Project project) {
        return super.save(project);
    }

    @Override
    @Transactional
    public boolean updateById(Project project) {
        return super.updateById(project);
    }

    @Override
    public PageVO<ProjectListVO> adminPage(PageDTO<Project> dto) {
        Project query = dto.getQuery();
        var wrapper = new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0);
        if (query != null && query.getGithubUrl() != null && !query.getGithubUrl().isBlank()) {
            wrapper.like(Project::getGithubUrl, query.getGithubUrl());
        }
        wrapper.orderByDesc(Project::getCreateTime);
        var page = PageUtil.<Project>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords().stream().map(this::toListVO).collect(Collectors.toList()));
    }

    @Override
    public ProjectListVO adminDetail(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) throw new BaseException("项目不存在");
        return toListVO(project);
    }

    // ==================== 访客端 ====================

    @Override
    public PageVO<ProjectListVO> publicPage(int pageNum, int pageSize) {
        var wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .orderByDesc(Project::getCreateTime);
        var page = Page.<Project>of(pageNum, pageSize);
        page(page, wrapper);
        List<ProjectListVO> rows = page.getRecords().stream().map(this::toListVO).collect(Collectors.toList());
        return new PageVO<>(page.getTotal(), rows);
    }

    // ==================== VO 组装 ====================

    private ProjectListVO toListVO(Project project) {
        ProjectListVO vo = new ProjectListVO();
        vo.setId(project.getId());
        vo.setGithubUrl(project.getGithubUrl());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());
        return vo;
    }
}