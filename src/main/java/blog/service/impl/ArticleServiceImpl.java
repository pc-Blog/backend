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

    public ArticleServiceImpl(ArticleTagMapper articleTagMapper, CategoryMapper categoryMapper,
                              TagMapper tagMapper) {
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
    }

    // ==================== 管理端 ====================

    @Override
    public PageVO<ArticleListVO> adminPage(PageDTO<Article> dto) {
        Article query = dto.getQuery();
        var wrapper = new LambdaQueryWrapper<Article>().eq(Article::getDeleted, 0);

        if (query != null) {
            if (query.getTitle() != null && !query.getTitle().isBlank())
                wrapper.like(Article::getTitle, query.getTitle());
            if (query.getCategoryId() != null)
                wrapper.eq(Article::getCategoryId, query.getCategoryId());
            if (query.getIsPublished() != null)
                wrapper.eq(Article::getIsPublished, query.getIsPublished());
        }

        wrapper.orderByDesc(Article::getIsPinned).orderByDesc(Article::getCreateTime);

        var page = PageUtil.<Article>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords().stream().map(this::toListVO).collect(Collectors.toList()));
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
            if (query.getTagId() != null) {
                wrapper.inSql(Article::getId, "SELECT article_id FROM t_article_tag WHERE tag_id = " + query.getTagId());
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

        return new PageVO<>(page.getTotal(), rows);
    }

    @Override
    public ArticleDetailVO adminDetail(Long id) {
        Article article = getById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BaseException("文章不存在");
        }
        ArticleDetailVO vo = new ArticleDetailVO();
        copyToListVO(article, vo);
        vo.setContent(article.getContent());
        return vo;
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
        vo.setUpdateTime(article.getUpdateTime());

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
