package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件归档（D1 → PostgreSQL 归档镜像）。
 *
 * <p>邮件主库在 Worker 的 D1，本表仅由同步任务拉取归档，供后台查看与备份，
 * 站点不直接读写。
 */
@Data
@TableName("t_email")
public class Email {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private String fromAddr;

    /** 发件人显示名 */
    private String fromName;

    private String toAddr;

    /** 收件人显示名 */
    private String toName;

    private String forwardTo;

    /** 收发方向：in 收件 / out 发件 */
    private String direction;

    private String subject;
    private String textBody;
    private String htmlBody;
    private String headers;
    private LocalDateTime createdAt;
}
