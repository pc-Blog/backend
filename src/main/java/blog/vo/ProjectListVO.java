package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectListVO {
    private Long id;
    private String githubUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}