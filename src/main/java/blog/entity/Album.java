package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "相册标题不能为空")
    @Size(max = 128, message = "相册标题不能超过128个字符")
    private String title;

    @Size(max = 512, message = "描述不能超过512个字符")
    private String description;

    private Integer sortOrder;
    private Integer isPublished;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
