package blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_project_tech")
public class ProjectTech {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long techId;
}
