package blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Size(max = 512, message = "项目简介不能超过512个字符")
    private String summary;

    private String content;
    private String coverImage;

    @NotNull(message = "项目分类不能为空")
    private Long categoryId;

    @TableField(exist = false)
    private List<Long> techIds;

    private String githubUrl;
    private String demoUrl;
    private Integer sortOrder;
    private Integer isPublished;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
