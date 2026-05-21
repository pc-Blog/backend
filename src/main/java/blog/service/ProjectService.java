package blog.service;

import blog.entity.Project;
import blog.vo.ProjectDetailVO;
import blog.vo.ProjectListVO;
import blog.common.PageDTO;
import blog.common.PageVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProjectService extends IService<Project> {
    PageVO<ProjectListVO> publicPage(int pageNum, int pageSize, Long categoryId, Long techId);

    PageVO<ProjectListVO> adminPage(PageDTO<Project> dto);

    ProjectDetailVO publicDetail(Long id);

    ProjectDetailVO adminDetail(Long id);

    void publish(Long id);

    void unpublish(Long id);
}
