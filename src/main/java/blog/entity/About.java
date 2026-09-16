package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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

    /**
     * 是否仅后端管理：0=前端可编辑 1=仅后端管理。
     *
     * <p>前端「关于」接口只暴露 {@code is_system=0} 的项，保存时也只覆盖这一批，
     * 因此后端自行维护的值（如音乐累计时长）不会被整页保存清掉。</p>
     */
    private Integer isSystem;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
