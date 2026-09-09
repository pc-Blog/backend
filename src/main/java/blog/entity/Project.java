package blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("t_project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 128, message = "项目名称不能超过128个字符")
    private String name;

    @Size(max = 512, message = "项目描述不能超过512个字符")
    private String summary;

    @NotBlank(message = "GitHub 仓库地址不能为空")
    @Size(max = 512, message = "GitHub 仓库地址不能超过512个字符")
    private String githubUrl;

    @TableField(exist = false)
    private List<Long> techIds;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}