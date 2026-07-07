package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_comment_upvote")
public class CommentUpvote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String subjectId;
    private Long userId;
    private LocalDateTime createdAt;
}
