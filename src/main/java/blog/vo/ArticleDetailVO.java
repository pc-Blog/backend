package blog.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailVO extends ArticleListVO {
    private String content;
    private ArticlePrevNextVO prev;
    private ArticlePrevNextVO next;
}
