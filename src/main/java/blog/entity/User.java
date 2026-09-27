package blog.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户（D1 → PostgreSQL 归档镜像）。
 *
 * <p>账号主库在 Worker 的 D1，本表由同步任务整表覆盖，同时支撑后台的用户管理与登录校验；
 * {@code password} 存 BCrypt 散列，后台登录依赖它，同步时会按 D1 覆盖。
 */
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String githubId;

    /**
     * 是否注销：0 正常 / 1 已注销。
     * <p>D1 的列名是 {@code deleted}，属性名对不上，Jackson 与 fastjson 都需要显式指明列名。
     */
    @TableField("deleted")
    @JsonProperty("deleted")
    @JSONField(name = "deleted")
    private Integer deletedFlag;

    /** 最后登录时间，D1 在登录成功时写入 */
    private LocalDateTime loginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
