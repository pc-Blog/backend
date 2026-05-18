package blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardVO {
    private long articleCount;
    private long projectCount;
    private long skillCount;
    private long timelineCount;
    private long commentCount;
    private long totalViews;
    private List<TopArticle> topArticles;
    private List<LatestComment> latestComments;

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

    @Data
    public static class LatestComment {
        private Long id;
        private String authorName;
        private String content;
        private LocalDateTime createTime;

        public LatestComment(Long id, String authorName, String content, LocalDateTime createTime) {
            this.id = id;
            this.authorName = authorName;
            this.content = content;
            this.createTime = createTime;
        }
    }
}
