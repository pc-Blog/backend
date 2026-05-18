package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.ArticleQueryDTO;
import blog.entity.*;
import blog.exception.BaseException;
import blog.mapper.*;
import blog.service.ArticleService;
import blog.util.PageUtil;
import blog.vo.ArticleDetailVO;
import blog.vo.ArticleListVO;
import blog.vo.ArticlePrevNextVO;
import blog.vo.ArticleTagVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;

    public ArticleServiceImpl(ArticleTagMapper articleTagMapper, CategoryMapper categoryMapper,
                              TagMapper tagMapper, CommentMapper commentMapper) {
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.commentMapper = commentMapper;
    }

    // ==================== 管理端 ====================

    @Override
    public PageVO<Article> page(PageDTO<Article> dto) {
        var page = PageUtil.<Article>toPage(dto);
        page(page);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional
    public boolean saveWithTags(Article article, List<Long> tagIds) {
        checkCategoryValid(article.getCategoryId());
        checkTagIdsValid(tagIds);
        if (article.getIsPublished() != null && article.getIsPublished() == 1) {
            article.setCreatedAt(LocalDateTime.now());
        }
        save(article);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateWithTags(Article article, List<Long> tagIds) {
        checkCategoryValid(article.getCategoryId());
        checkTagIdsValid(tagIds);
        Article existing = getById(article.getId());
        if (existing != null && existing.getIsPublished() == 0
                && article.getIsPublished() != null && article.getIsPublished() == 1) {
            article.setCreatedAt(LocalDateTime.now());
        }
        updateById(article);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
        return true;
    }

    @Override
    public void publish(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BaseException("文章不存在");
        }
        var update = new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getIsPublished, 1);
        if (article.getCreatedAt() == null) {
            update.set(Article::getCreatedAt, LocalDateTime.now());
        }
        update(update);
    }

    @Override
    public void unpublish(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BaseException("文章不存在");
        }
        update(new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getIsPublished, 0));
    }

    @Override
    public void togglePin(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BaseException("文章不存在");
        }
        update(new LambdaUpdateWrapper<Article>()
                .eq(Article::getId, id)
                .set(Article::getIsPinned, article.getIsPinned() == null || article.getIsPinned() == 0 ? 1 : 0));
    }

    @Override
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
    }

    // ==================== 访客端 ====================

    @Override
    public PageVO<ArticleListVO> publicPage(int pageNum, int pageSize, ArticleQueryDTO query) {
        var wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, 0)
                .eq(Article::getIsPublished, 1);

        if (query != null) {
            if (query.getCategoryId() != null) {
                wrapper.eq(Article::getCategoryId, query.getCategoryId());
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                wrapper.and(w -> w.like(Article::getTitle, query.getKeyword())
                        .or().like(Article::getSummary, query.getKeyword()));
            }
        }

        wrapper.orderByDesc(Article::getIsPinned)
                .orderByDesc(Article::getCreatedAt);

        var page = Page.<Article>of(pageNum, pageSize);
        page(page, wrapper);

        List<ArticleListVO> rows = page.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        // tagId 筛选: 先查所有，再按标签过滤
        if (query != null && query.getTagId() != null) {
            rows = rows.stream()
                    .filter(vo -> vo.getTags().stream().anyMatch(t -> t.getId().equals(query.getTagId())))
                    .collect(Collectors.toList());
        }

        return new PageVO<>(page.getTotal(), rows);
    }

    @Override
    public ArticleDetailVO publicDetail(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() == 1 || article.getIsPublished() != 1) {
            throw new BaseException("文章不存在或未发布");
        }

        ArticleDetailVO vo = new ArticleDetailVO();
        copyToListVO(article, vo);
        vo.setContent(article.getContent());

        // prev/next
        var prevArticle = getOne(new LambdaQueryWrapper<Article>()
                .lt(Article::getId, id)
                .eq(Article::getDeleted, 0)
                .eq(Article::getIsPublished, 1)
                .orderByDesc(Article::getId)
                .last("LIMIT 1"));
        vo.setPrev(prevArticle != null ? new ArticlePrevNextVO(prevArticle.getId(), prevArticle.getTitle()) : null);

        var nextArticle = getOne(new LambdaQueryWrapper<Article>()
                .gt(Article::getId, id)
                .eq(Article::getDeleted, 0)
                .eq(Article::getIsPublished, 1)
                .orderByAsc(Article::getId)
                .last("LIMIT 1"));
        vo.setNext(nextArticle != null ? new ArticlePrevNextVO(nextArticle.getId(), nextArticle.getTitle()) : null);

        return vo;
    }

    // ==================== 组装 VO ====================

    private ArticleListVO toListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        copyToListVO(article, vo);
        return vo;
    }

    private void copyToListVO(Article article, ArticleListVO vo) {
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryId(article.getCategoryId());
        vo.setIsPinned(article.getIsPinned());
        vo.setIsPublished(article.getIsPublished());
        vo.setViewCount(article.getViewCount());
        vo.setCreatedAt(article.getCreatedAt());

        // 分类名
        Category category = categoryMapper.selectById(article.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : null);

        // 标签
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTags(tags.stream()
                    .filter(t -> t.getDeleted() == 0)
                    .map(t -> new ArticleTagVO(t.getId(), t.getName()))
                    .collect(Collectors.toList()));
        } else {
            vo.setTags(new ArrayList<>());
        }

        // 评论数
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, article.getId())
                        .eq(Comment::getDeleted, 0));
        vo.setCommentCount(commentCount);
    }

    // ==================== 校验 ====================

    private void checkCategoryValid(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BaseException("文章分类不存在");
        }
        if (!"ARTICLE".equals(category.getType())) {
            throw new BaseException("所选分类不是文章类型");
        }
    }

    private void checkTagIdsValid(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null || tag.getDeleted() == 1) {
                throw new BaseException("标签不存在: id=" + tagId);
            }
        }
    }
}
