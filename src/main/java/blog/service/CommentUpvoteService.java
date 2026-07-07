package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.CommentUpvote;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CommentUpvoteService extends IService<CommentUpvote> {
    PageVO<CommentUpvote> page(PageDTO<CommentUpvote> dto);
}
