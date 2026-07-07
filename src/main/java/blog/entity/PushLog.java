package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_push_log")
public class PushLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime pushedAt;
    private Integer articleCount;
    private Integer subscriberCount;
    private String groupName;
    private String status;
    private String errorMsg;
    private String articleIds;
}
