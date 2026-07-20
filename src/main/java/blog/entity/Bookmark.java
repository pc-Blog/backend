package blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_bookmark")
public class Bookmark {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "网站名称不能为空")
    @Size(max = 100, message = "网站名称不能超过100个字符")
    private String name;

    @NotBlank(message = "网站链接不能为空")
    @Size(max = 500, message = "网站链接不能超过500个字符")
    private String url;

    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;

    @Size(max = 500, message = "图标链接不能超过500个字符")
    private String icon;

    private Long categoryId;
    private Integer isPin;
    private Integer sortOrder;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
