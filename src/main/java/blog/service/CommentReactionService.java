package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.CommentReaction;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CommentReactionService extends IService<CommentReaction> {
    PageVO<CommentReaction> page(PageDTO<CommentReaction> dto);
}
