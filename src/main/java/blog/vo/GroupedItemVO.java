package blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupedItemVO {
    /** "article" 或 "series" */
    private String type;

    // 当 type = "article" 时有效
    private ArticleListVO article;

    // 当 type = "series" 时有效
    private String series;
    private Long seriesArticleCount;
    private List<ArticleListVO> seriesArticles;

    public static GroupedItemVO article(ArticleListVO a) {
        GroupedItemVO vo = new GroupedItemVO();
        vo.setType("article");
        vo.setArticle(a);
        return vo;
    }

    public static GroupedItemVO series(String name, List<ArticleListVO> articles) {
        GroupedItemVO vo = new GroupedItemVO();
        vo.setType("series");
        vo.setSeries(name);
        vo.setSeriesArticleCount((long) articles.size());
        vo.setSeriesArticles(articles);
        return vo;
    }
}
