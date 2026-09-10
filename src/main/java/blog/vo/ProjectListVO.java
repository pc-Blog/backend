package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectListVO {
    private Long id;
    private String githubUrl;
    private List<TechnologyVO> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}