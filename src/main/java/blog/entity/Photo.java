package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_photo")
public class Photo {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "所属相册不能为空")
    private Long albumId;

    @NotBlank(message = "图片URL不能为空")
    @Size(max = 512, message = "图片URL不能超过512个字符")
    private String url;

    private Integer sortOrder;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
