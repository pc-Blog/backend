package blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文学作品
 */
@Data
@TableName("t_literature")
public class Literature {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "作品标题不能为空")
    @Size(max = 256, message = "作品标题不能超过256个字符")
    private String title;

    @NotBlank(message = "作品内容不能为空")
    private String content;

    /** 分类ID，指向 t_category 中 type='LITERATURE' 的记录 */
    private Long categoryId;

    /** 写作日期 */
    private LocalDate writtenAt;

    /** 写作时天气 */
    @Size(max = 50, message = "天气不能超过50个字符")
    private String weather;

    private Integer isPublished;

    @TableLogic(value = "0", delval = "1")
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
