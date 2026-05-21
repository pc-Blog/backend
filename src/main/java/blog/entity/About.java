package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_about")
public class About {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String itemKey;
    private String itemValue;
    private Integer sortOrder;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
