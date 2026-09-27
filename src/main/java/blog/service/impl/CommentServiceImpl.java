package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.Comment;
import blog.mapper.CommentMapper;
import blog.service.CommentService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Override
    public PageVO<Comment> page(PageDTO<Comment> dto) {
        var wrapper = new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime);
        var page = PageUtil.<Comment>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
