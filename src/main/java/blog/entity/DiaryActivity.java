package blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日记活动条目（一天多条）
 *
 * <p>编辑日记时整天整体替换，因此没有 update_time。</p>
 */
@Data
@TableName("t_diary_activity")
public class DiaryActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属日记ID（t_diary.id） */
    private Long diaryId;

    /** 活动内容 */
    private String activity;

    /** 分类枚举值：1=学习 2=工作 3=生活 4=运动 5=娱乐 6=社交 */
    private Integer category;

    /** 小分类名称（字符串而非外键：删除选项不影响历史记录） */
    private String subcategory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
