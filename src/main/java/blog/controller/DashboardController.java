package blog.controller;

import blog.common.Result;
import blog.entity.Article;
import blog.entity.Comment;
import blog.entity.Project;
import blog.entity.Skill;
import blog.entity.Timeline;
import blog.mapper.ArticleMapper;
import blog.mapper.CommentMapper;
import blog.mapper.ProjectMapper;
import blog.mapper.SkillMapper;
import blog.mapper.TimelineMapper;
import blog.vo.DashboardVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final ArticleMapper articleMapper;
    private final ProjectMapper projectMapper;
    private final SkillMapper skillMapper;
    private final TimelineMapper timelineMapper;
    private final CommentMapper commentMapper;

    public DashboardController(ArticleMapper articleMapper, ProjectMapper projectMapper,
                               SkillMapper skillMapper, TimelineMapper timelineMapper,
                               CommentMapper commentMapper) {
        this.articleMapper = articleMapper;
        this.projectMapper = projectMapper;
        this.skillMapper = skillMapper;
        this.timelineMapper = timelineMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        log.info("查询数据看板");
        DashboardVO vo = new DashboardVO();

        vo.setArticleCount(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0).eq(Article::getIsPublished, 1)));
        vo.setProjectCount(projectMapper.selectCount(
                new LambdaQueryWrapper<Project>().eq(Project::getDeleted, 0).eq(Project::getIsPublished, 1)));
        vo.setSkillCount(skillMapper.selectCount(
                new LambdaQueryWrapper<Skill>().eq(Skill::getDeleted, 0)));
        vo.setTimelineCount(timelineMapper.selectCount(
                new LambdaQueryWrapper<Timeline>().eq(Timeline::getDeleted, 0)));
        vo.setCommentCount(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getDeleted, 0)));

        // 总阅读量
        Long sum = articleMapper.selectObjs(
                new LambdaQueryWrapper<Article>().select(Article::getViewCount).eq(Article::getDeleted, 0))
                .stream().mapToLong(o -> o != null ? ((Number) o).longValue() : 0).sum();
        vo.setTotalViews(sum);

        // Top5 阅读量
        List<Article> topArticles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0).eq(Article::getIsPublished, 1)
                        .orderByDesc(Article::getViewCount).last("LIMIT 5"));
        vo.setTopArticles(topArticles.stream()
                .map(a -> new DashboardVO.TopArticle(a.getId(), a.getTitle(), a.getViewCount()))
                .collect(Collectors.toList()));

        // 最新5条评论
        List<Comment> latestComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getDeleted, 0)
                        .orderByDesc(Comment::getCreateTime).last("LIMIT 5"));
        vo.setLatestComments(latestComments.stream()
                .map(c -> new DashboardVO.LatestComment(c.getId(), c.getAuthorName(), c.getContent(), c.getCreateTime()))
                .collect(Collectors.toList()));

        return Result.success(vo);
    }
}
