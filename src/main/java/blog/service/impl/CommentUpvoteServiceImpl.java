package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.CommentUpvote;
import blog.mapper.CommentUpvoteMapper;
import blog.service.CommentUpvoteService;
import blog.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentUpvoteServiceImpl extends ServiceImpl<CommentUpvoteMapper, CommentUpvote> implements CommentUpvoteService {
    @Override
    public PageVO<CommentUpvote> page(PageDTO<CommentUpvote> dto) {
        var wrapper = new LambdaQueryWrapper<CommentUpvote>().orderByDesc(CommentUpvote::getCreatedAt);
        var page = PageUtil.<CommentUpvote>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }
}
