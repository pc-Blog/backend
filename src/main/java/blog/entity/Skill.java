package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_skill")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "技能名称不能为空")
    @Size(max = 64, message = "技能名称不能超过64个字符")
    private String name;

    private String category;

    @Min(value = 0, message = "熟练度不能小于0")
    @Max(value = 100, message = "熟练度不能大于100")
    private Integer proficiency;

    private Integer sortOrder;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
