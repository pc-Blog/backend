package blog.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpTag {
    private Long id;
    private String name;
    private List<OpArticle> articles = new ArrayList<>();
}
