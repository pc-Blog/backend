package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.*;
import blog.exception.BaseException;
import blog.mapper.*;
import blog.util.PageUtil;
import blog.service.ProjectService;
import blog.vo.ProjectDetailVO;
import blog.vo.ProjectListVO;
import blog.vo.TechnologyVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final CategoryMapper categoryMapper;
    private final ProjectTechMapper projectTechMapper;
    private final TechnologyMapper technologyMapper;

    public ProjectServiceImpl(CategoryMapper categoryMapper, ProjectTechMapper projectTechMapper,
                              TechnologyMapper technologyMapper) {
        this.categoryMapper = categoryMapper;
        this.projectTechMapper = projectTechMapper;
        this.technologyMapper = technologyMapper;
    }

    // ==================== 管理端 ====================

    @Override
    @Transactional
    public boolean save(Project project) {
        checkCategoryValid(project.getCategoryId());
        super.save(project);
        saveTechRelations(project.getId(), project.getTechIds());
        return true;
    }

    @Override
    @Transactional
    public boolean updateById(Project project) {
        checkCategoryValid(project.getCategoryId());
        super.updateById(project);
        projectTechMapper.delete(new LambdaQueryWrapper<ProjectTech>().eq(ProjectTech::getProjectId, project.getId()));
        saveTechRelations(project.getId(), project.getTechIds());
        return true;
    }

    @Override
    public void publish(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) throw new BaseException("项目不存在");
        update(new LambdaUpdateWrapper<Project>().eq(Project::getId, id).set(Project::getIsPublished, 1));
    }

    @Override
    public void unpublish(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) throw new BaseException("项目不存在");
        update(new LambdaUpdateWrapper<Project>().eq(Project::getId, id).set(Project::getIsPublished, 0));
    }

    @Override
    public PageVO<ProjectListVO> adminPage(PageDTO<Project> dto) {
        Project query = dto.getQuery();
        var wrapper = new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0);
        if (query != null) {
            if (query.getName() != null && !query.getName().isBlank())
                wrapper.like(Project::getName, query.getName());
            if (query.getCategoryId() != null)
                wrapper.eq(Project::getCategoryId, query.getCategoryId());
            if (query.getIsPublished() != null)
                wrapper.eq(Project::getIsPublished, query.getIsPublished());
        }
        wrapper.orderByAsc(Project::getSortOrder).orderByDesc(Project::getCreateTime);
        var page = PageUtil.<Project>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords().stream().map(this::toListVO).collect(Collectors.toList()));
    }

    @Override
    public ProjectDetailVO adminDetail(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) throw new BaseException("项目不存在");
        ProjectDetailVO vo = new ProjectDetailVO();
        copyToListVO(project, vo);
        vo.setContent(project.getContent());
        return vo;
    }

    // ==================== 访客端 ====================

    @Override
    public PageVO<ProjectListVO> publicPage(int pageNum, int pageSize, Long categoryId, Long techId) {
        var wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1);
        if (categoryId != null) wrapper.eq(Project::getCategoryId, categoryId);
        wrapper.orderByAsc(Project::getSortOrder).orderByDesc(Project::getCreateTime);

        var page = Page.<Project>of(pageNum, pageSize);
        page(page, wrapper);

        List<ProjectListVO> rows = page.getRecords().stream().map(this::toListVO).collect(Collectors.toList());

        if (techId != null) {
            rows = rows.stream()
                    .filter(vo -> vo.getTechs().stream().anyMatch(t -> t.getId().equals(techId)))
                    .collect(Collectors.toList());
        }
        return new PageVO<>(page.getTotal(), rows);
    }

    @Override
    public ProjectDetailVO publicDetail(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1 || project.getIsPublished() != 1)
            throw new BaseException("项目不存在或未发布");

        ProjectDetailVO vo = new ProjectDetailVO();
        copyToListVO(project, vo);
        vo.setContent(project.getContent());

        var prevProj = getOne(new LambdaQueryWrapper<Project>()
                .lt(Project::getId, id).eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1)
                .orderByDesc(Project::getId).last("LIMIT 1"));
        vo.setPrev(prevProj != null ? new ProjectDetailVO.ProjectPrevNextVO(prevProj.getId(), prevProj.getName()) : null);

        var nextProj = getOne(new LambdaQueryWrapper<Project>()
                .gt(Project::getId, id).eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1)
                .orderByAsc(Project::getId).last("LIMIT 1"));
        vo.setNext(nextProj != null ? new ProjectDetailVO.ProjectPrevNextVO(nextProj.getId(), nextProj.getName()) : null);

        return vo;
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
        copyToListVO(project, vo);
        return vo;
    }

    private void copyToListVO(Project project, ProjectListVO vo) {
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setSummary(project.getSummary());
        vo.setCoverImage(project.getCoverImage());
        vo.setCategoryId(project.getCategoryId());
        vo.setGithubUrl(project.getGithubUrl());
        vo.setDemoUrl(project.getDemoUrl());
        vo.setSortOrder(project.getSortOrder());
        vo.setIsPublished(project.getIsPublished());
        vo.setCreateTime(project.getCreateTime());

        Category category = categoryMapper.selectById(project.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);

        List<ProjectTech> ptList = projectTechMapper.selectList(
                new LambdaQueryWrapper<ProjectTech>().eq(ProjectTech::getProjectId, project.getId()));
        if (ptList != null && !ptList.isEmpty()) {
            List<Long> techIds = ptList.stream().map(ProjectTech::getTechId).collect(Collectors.toList());
            List<Technology> techs = technologyMapper.selectBatchIds(techIds);
            vo.setTechs(techs.stream().filter(t -> t.getDeleted() == 0)
                    .map(t -> new TechnologyVO(t.getId(), t.getName())).collect(Collectors.toList()));
        } else {
            vo.setTechs(new ArrayList<>());
        }
    }

    private void checkCategoryValid(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) throw new BaseException("项目分类不存在");
        if (!"PROJECT".equals(category.getType())) throw new BaseException("所选分类不是项目类型");
    }
}
