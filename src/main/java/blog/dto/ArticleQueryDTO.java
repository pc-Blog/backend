package blog.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    private Long categoryId;
    private Long tagId;
    private String keyword;
    private Boolean isPublished;
}
