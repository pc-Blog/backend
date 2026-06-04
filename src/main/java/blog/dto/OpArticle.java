package blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpArticle {
    private Long id;
    private String title;
    private String content;
    private String writtenAt;
    private List<Long> tagIds;
}
