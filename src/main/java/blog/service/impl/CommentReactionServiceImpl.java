package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.CommentReaction;
import blog.mapper.CommentReactionMapper;
import blog.service.CommentReactionService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentReactionServiceImpl extends ServiceImpl<CommentReactionMapper, CommentReaction> implements CommentReactionService {
    @Override
    public PageVO<CommentReaction> page(PageDTO<CommentReaction> dto) {
        var wrapper = new LambdaQueryWrapper<CommentReaction>().orderByDesc(CommentReaction::getCreatedAt);
        var page = PageUtil.<CommentReaction>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
