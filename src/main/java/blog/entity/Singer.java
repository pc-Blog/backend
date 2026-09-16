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
 * 歌手
 *
 * <p>音频文件本体登记在 {@code t_media}，本表只存业务元信息。
 * 不设空名占位行：未分配歌手的歌曲令 {@code t_music.singer_id} 为 NULL。
 * 封面为空时由前端回落到默认封面代码常量。</p>
 */
@Data
@TableName("t_singer")
public class Singer {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "歌手名称不能为空")
    @Size(max = 100, message = "歌手名称不能超过100个字符")
    private String name;

    /** 歌手封面URL，为空时前端回落默认图 */
    @Size(max = 500, message = "封面URL不能超过500个字符")
    private String pictureUrl;

    @TableLogic(value = "0", delval = "1")
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
