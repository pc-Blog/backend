package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Category;
import blog.entity.Project;
import blog.exception.BaseException;
import blog.mapper.CategoryMapper;
import blog.mapper.ProjectMapper;
import blog.service.ProjectService;
import blog.util.PageUtil;
import blog.vo.ProjectDetailVO;
import blog.vo.ProjectListVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private final CategoryMapper categoryMapper;

    public ProjectServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    // ==================== 管理端 ====================

    @Override
    public PageVO<Project> page(PageDTO<Project> dto) {
        var page = PageUtil.<Project>toPage(dto);
        page(page);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(Project project) {
        checkCategoryValid(project.getCategoryId());
        return super.save(project);
    }

    @Override
    public boolean updateById(Project project) {
        checkCategoryValid(project.getCategoryId());
        return super.updateById(project);
    }

    @Override
    public void publish(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) {
            throw new BaseException("项目不存在");
        }
        update(new LambdaUpdateWrapper<Project>()
                .eq(Project::getId, id)
                .set(Project::getIsPublished, 1));
    }

    @Override
    public void unpublish(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1) {
            throw new BaseException("项目不存在");
        }
        update(new LambdaUpdateWrapper<Project>()
                .eq(Project::getId, id)
                .set(Project::getIsPublished, 0));
    }

    // ==================== 访客端 ====================

    @Override
    public PageVO<ProjectListVO> publicPage(int pageNum, int pageSize, Long categoryId) {
        var wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getDeleted, 0)
                .eq(Project::getIsPublished, 1);
        if (categoryId != null) {
            wrapper.eq(Project::getCategoryId, categoryId);
        }
        wrapper.orderByAsc(Project::getSortOrder).orderByDesc(Project::getCreateTime);

        var page = Page.<Project>of(pageNum, pageSize);
        page(page, wrapper);

        List<ProjectListVO> rows = page.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());
        return new PageVO<>(page.getTotal(), rows);
    }

    @Override
    public ProjectDetailVO publicDetail(Long id) {
        Project project = getById(id);
        if (project == null || project.getDeleted() == 1 || project.getIsPublished() != 1) {
            throw new BaseException("项目不存在或未发布");
        }

        ProjectDetailVO vo = new ProjectDetailVO();
        copyToListVO(project, vo);
        vo.setContent(project.getContent());

        // prev/next
        var prevProj = getOne(new LambdaQueryWrapper<Project>()
                .lt(Project::getId, id)
                .eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1)
                .orderByDesc(Project::getId).last("LIMIT 1"));
        vo.setPrev(prevProj != null ? new ProjectDetailVO.ProjectPrevNextVO(prevProj.getId(), prevProj.getName()) : null);

        var nextProj = getOne(new LambdaQueryWrapper<Project>()
                .gt(Project::getId, id)
                .eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1)
                .orderByAsc(Project::getId).last("LIMIT 1"));
        vo.setNext(nextProj != null ? new ProjectDetailVO.ProjectPrevNextVO(nextProj.getId(), nextProj.getName()) : null);

        return vo;
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
        vo.setTechStack(project.getTechStack());
        vo.setGithubUrl(project.getGithubUrl());
        vo.setDemoUrl(project.getDemoUrl());
        vo.setSortOrder(project.getSortOrder());
        vo.setIsPublished(project.getIsPublished());
        vo.setCreateTime(project.getCreateTime());

        Category category = categoryMapper.selectById(project.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);
    }

    private void checkCategoryValid(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BaseException("项目分类不存在");
        }
        if (!"PROJECT".equals(category.getType())) {
            throw new BaseException("所选分类不是项目类型");
        }
    }
}
