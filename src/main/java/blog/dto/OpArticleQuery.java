package blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpArticleQuery {
    private String title;
    private Long tagId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
