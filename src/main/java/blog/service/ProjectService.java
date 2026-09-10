package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Project;
import blog.vo.ProjectListVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProjectService extends IService<Project> {
    PageVO<ProjectListVO> publicPage(int pageNum, int pageSize);

    PageVO<ProjectListVO> adminPage(PageDTO<Project> dto);

    ProjectListVO adminDetail(Long id);
}