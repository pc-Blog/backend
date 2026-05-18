package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectListVO {
    private Long id;
    private String name;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private String techStack;
    private String githubUrl;
    private String demoUrl;
    private Integer sortOrder;
    private Integer isPublished;
    private LocalDateTime createTime;
}
