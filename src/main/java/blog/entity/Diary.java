package blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日记日期（一天一行）
 *
 * <p>仅本地使用，不参与静态数据同步。一天只能有一篇，由
 * {@code uk_diary_record_date} 唯一索引保证。</p>
 */
@Data
@TableName("t_diary")
public class Diary {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 记录日期，唯一 */
    private LocalDate recordDate;

    /** 天气枚举值：1=晴 2=多云 3=阴 4=小雨 5=大雨 9=中雨 6=雪 7=雾 8=雷 */
    private Integer weather;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 当天的活动条目。
     *
     * <p>非数据库字段，仅用于组装响应；落库在 t_diary_activity。</p>
     */
    @TableField(exist = false)
    private List<DiaryActivity> activities;
}
