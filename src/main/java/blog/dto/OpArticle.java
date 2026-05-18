package blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpArticle {
    private Long id;
    private String title;
    private String fileName;
    private String weather;
    private String writtenAt;
    private List<Long> tagIds;
}
