package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Project;
import blog.vo.ProjectDetailVO;
import blog.vo.ProjectListVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProjectService extends IService<Project> {
    PageVO<Project> page(PageDTO<Project> dto);

    PageVO<ProjectListVO> publicPage(int pageNum, int pageSize, Long categoryId);

    ProjectDetailVO publicDetail(Long id);

    void publish(Long id);

    void unpublish(Long id);
}
