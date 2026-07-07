package blog.mapper;

import blog.entity.CommentUpvote;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentUpvoteMapper extends BaseMapper<CommentUpvote> {
}
