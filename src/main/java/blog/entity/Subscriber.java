package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_subscriber")
public class Subscriber {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String groupName;
    private LocalDateTime createdAt;
}
