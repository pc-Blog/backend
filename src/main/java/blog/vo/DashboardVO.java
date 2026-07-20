package blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {
    private long articleCount;
    private long projectCount;
    private long skillCount;
    private long timelineCount;
    private long totalViews;
    private List<TopArticle> topArticles;

    @Data
    public static class TopArticle {
        private Long id;
        private String title;
        private long viewCount;

        public TopArticle(Long id, String title, Long viewCount) {
            this.id = id;
            this.title = title;
            this.viewCount = viewCount != null ? viewCount : 0;
        }
    }

}
