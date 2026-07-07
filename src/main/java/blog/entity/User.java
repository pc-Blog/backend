package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 64, message = "用户名长度需在2-64之间")
    private String username;

    @Size(min = 6, max = 128, message = "密码长度需在6-128之间")
    private String password;

    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    private String avatar;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String githubId;

    /** 以下三个字段用于 Worker D1 数据同步 */
    private String githubToken;
    private String githubRefreshToken;
    private String githubTokenExpiresAt;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
