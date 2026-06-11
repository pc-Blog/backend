package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@TableName("t_chatter_image")
public class ChatterImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull
    private Long chatterId;

    @NotBlank
    @Size(max = 500)
    private String url;

    private Integer sortOrder;
}
