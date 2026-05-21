package blog.service;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.dto.ArticleQueryDTO;
import blog.entity.Article;
import blog.vo.ArticleDetailVO;
import blog.vo.ArticleListVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ArticleService extends IService<Article> {
    PageVO<ArticleListVO> adminPage(PageDTO<Article> dto);

    boolean saveWithTags(Article article, List<Long> tagIds);

    boolean updateWithTags(Article article, List<Long> tagIds);

    PageVO<ArticleListVO> publicPage(int pageNum, int pageSize, ArticleQueryDTO query);

    ArticleDetailVO publicDetail(Long id);

    ArticleDetailVO adminDetail(Long id);

    void incrementViewCount(Long id);

    void publish(Long id);

    void unpublish(Long id);

    void togglePin(Long id);
}
