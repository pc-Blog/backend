package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Project;
import blog.entity.ProjectTech;
import blog.entity.Technology;
import blog.exception.BaseException;
import blog.mapper.ProjectMapper;
import blog.mapper.ProjectTechMapper;
import blog.mapper.TechnologyMapper;
import blog.service.ProjectService;
import blog.util.PageUtil;
import blog.vo.ProjectListVO;
import blog.vo.TechnologyVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final ProjectTechMapper projectTechMapper;
    private final TechnologyMapper technologyMapper;

    public ProjectServiceImpl(ProjectTechMapper projectTechMapper, TechnologyMapper technologyMapper) {
        this.projectTechMapper = projectTechMapper;
        this.technologyMapper = technologyMapper;
    }

    // ==================== 管理端 ====================

    @Override
    @Transactional
    public boolean save(Project project) {
        super.save(project);
        saveTechRelations(project.getId(), project.getTechIds());
        return true;
    }

    @Override
    @Transactional
    public boolean updateById(Project project) {
        super.updateById(project);
        projectTechMapper.delete(new LambdaQueryWrapper<ProjectTech>().eq(ProjectTech::getProjectId, project.getId()));
        saveTechRelations(project.getId(), project.getTechIds());
        return true;
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
    public PageVO<ProjectListVO> publicPage(int pageNum, int pageSize, Long techId) {
        var wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .orderByDesc(Project::getCreateTime);

        var page = Page.<Project>of(pageNum, pageSize);
        page(page, wrapper);

        List<ProjectListVO> rows = page.getRecords().stream().map(this::toListVO).collect(Collectors.toList());

        if (techId != null) {
            rows = rows.stream()
                    .filter(vo -> vo.getTags().stream().anyMatch(t -> t.getId().equals(techId)))
                    .collect(Collectors.toList());
        }
        return new PageVO<>(page.getTotal(), rows);
    }

    // ==================== 内部 ====================

    private void saveTechRelations(Long projectId, List<Long> techIds) {
        if (techIds == null || techIds.isEmpty()) return;
        for (Long techId : techIds) {
            ProjectTech pt = new ProjectTech();
            pt.setProjectId(projectId);
            pt.setTechId(techId);
            projectTechMapper.insert(pt);
        }
    }

    // ==================== VO 组装 ====================

    private ProjectListVO toListVO(Project project) {
        ProjectListVO vo = new ProjectListVO();
        vo.setId(project.getId());
        vo.setGithubUrl(project.getGithubUrl());
        vo.setCreateTime(project.getCreateTime());
        vo.setUpdateTime(project.getUpdateTime());

        List<ProjectTech> ptList = projectTechMapper.selectList(
                new LambdaQueryWrapper<ProjectTech>().eq(ProjectTech::getProjectId, project.getId()));
        if (ptList != null && !ptList.isEmpty()) {
            List<Long> techIds = ptList.stream().map(ProjectTech::getTechId).collect(Collectors.toList());
            List<Technology> techs = technologyMapper.selectBatchIds(techIds);
            vo.setTags(techs.stream().filter(t -> t.getDeleted() == 0)
                    .map(t -> new TechnologyVO(t.getId(), t.getName())).collect(Collectors.toList()));
        } else {
            vo.setTags(new ArrayList<>());
        }
        return vo;
    }
}