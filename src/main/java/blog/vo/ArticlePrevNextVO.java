package blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticlePrevNextVO {
    private Long id;
    private String title;
}
