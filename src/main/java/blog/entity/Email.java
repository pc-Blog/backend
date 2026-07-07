package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_email")
public class Email {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private String fromAddr;
    private String toAddr;
    private String forwardTo;
    private String subject;
    private String textBody;
    private String htmlBody;
    private String headers;
    private LocalDateTime createdAt;
}
