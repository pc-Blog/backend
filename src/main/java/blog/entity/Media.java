package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_media")
public class Media {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "文件名不能为空")
    private String filename;

    private String originalFilename;
    private String filePath;

    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    @PositiveOrZero(message = "文件大小不能为负数")
    private Long fileSize;

    @NotBlank(message = "MIME类型不能为空")
    private String mimeType;

    private String relationType;

    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
