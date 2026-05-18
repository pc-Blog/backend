package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "评论所属文章不能为空")
    private Long articleId;

    @Size(max = 64, message = "评论昵称不能超过64个字符")
    private String authorName;

    @Email(message = "邮箱格式不正确")
    private String authorEmail;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long userId;
    private String userAgent;
    private String ipAddress;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
