package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.ArticleQueryDTO;
import blog.dto.ArticleSaveRequest;
import blog.entity.Article;
import blog.service.ArticleService;
import blog.vo.ArticleDetailVO;
import blog.vo.ArticleListVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // ==================== 访客端 ====================

    @PostMapping("/public/page")
    public Result<PageVO<ArticleListVO>> publicPage(@RequestBody PageDTO<ArticleQueryDTO> dto) {
        log.info("访客端分页查询文章:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(articleService.publicPage(dto.getPageNum(), dto.getPageSize(), dto.getQuery()));
    }

    @GetMapping("/public/{id}")
    public Result<ArticleDetailVO> publicDetail(@PathVariable Long id) {
        log.info("访客端查看文章详情, id:{}", id);
        ArticleDetailVO detail = articleService.publicDetail(id);
        return Result.success(detail);
    }

    @PutMapping("/{id}/view")
    public Result<Void> view(@PathVariable Long id) {
        log.info("增加文章阅读量, id:{}", id);
        articleService.incrementViewCount(id);
        return Result.success();
    }

    // ==================== 管理端 ====================

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        log.info("根据ID查询文章, id:{}", id);
        Article article = articleService.getById(id);
        if (article == null) return Result.error("文章不存在");
        return Result.success(articleService.adminDetail(id));
    }

    @PostMapping
    public Result<Article> save(@Valid @RequestBody ArticleSaveRequest request) {
        log.info("新增文章:{}", JSON.toJSONString(request, SerializerFeature.PrettyFormat));
        articleService.saveWithTags(request, request.getTagIds());
        return Result.success(request);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ArticleSaveRequest request) {
        log.info("更新文章:{}", JSON.toJSONString(request, SerializerFeature.PrettyFormat));
        articleService.updateWithTags(request, request.getTagIds());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除文章, id:{}", id);
        articleService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<ArticleListVO>> page(@RequestBody PageDTO<Article> dto) {
        log.info("管理端分页查询文章:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(articleService.adminPage(dto));
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布文章, id:{}", id);
        articleService.publish(id);
        return Result.success();
    }

    @PutMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        log.info("下架文章, id:{}", id);
        articleService.unpublish(id);
        return Result.success();
    }

    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id) {
        log.info("切换文章置顶, id:{}", id);
        articleService.togglePin(id);
        return Result.success();
    }
}
