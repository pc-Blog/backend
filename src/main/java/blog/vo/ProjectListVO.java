package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectListVO {
    private Long id;
    private String name;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private List<TechnologyVO> techs;
    private String githubUrl;
    private String demoUrl;
    private Integer sortOrder;
    private Integer isPublished;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
