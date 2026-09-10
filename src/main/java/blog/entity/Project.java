package blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "GitHub 仓库地址不能为空")
    @Size(max = 512, message = "GitHub 仓库地址不能超过512个字符")
    private String githubUrl;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}