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

import java.time.LocalDateTime;

/**
 * 音乐
 *
 * <p>音频文件本体登记在 {@code t_media}（relation_type='music'），此处
 * {@code file_url} 为引用副本，业务读取时不查 {@code t_media}。</p>
 *
 * <p>注意：删歌只置 {@code deleted=1}，不动 {@code t_media}、不动 MinIO 文件。
 * 因此收集引用（前端孤儿扫描）时必须过滤 {@code deleted=0}，否则已删曲目的
 * 文件会被判为「仍被引用」而永远清不掉。</p>
 */
@Data
@TableName("t_music")
public class Music {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "歌曲标题不能为空")
    @Size(max = 512, message = "歌曲标题不能超过512个字符")
    private String title;

    /** 音频文件URL（引用副本，权威登记在 t_media） */
    @NotBlank(message = "文件URL不能为空")
    @Size(max = 500, message = "文件URL不能超过500个字符")
    private String fileUrl;

    /** 时长（秒） */
    private Integer duration;

    private Integer playCount;

    private Boolean isFavorite;

    private LocalDateTime lastPlayed;

    /** 歌手ID，NULL 表示未分配歌手 */
    private Long singerId;

    /** 分类ID，NULL 表示未分配分类 */
    private Long categoryId;

    @TableLogic(value = "0", delval = "1")
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
