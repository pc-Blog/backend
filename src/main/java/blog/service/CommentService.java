package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CommentService extends IService<Comment> {
    PageVO<Comment> page(PageDTO<Comment> dto);
}
