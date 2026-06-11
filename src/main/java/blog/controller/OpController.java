package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.OpArticleQuery;
import blog.dto.OpArticle;
import blog.dto.OpMusic;
import blog.dto.OpTag;
import blog.exception.BaseException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@RequestMapping("/api/op")
@RestController
public class OpController {
    private final WebClient opClient;  // 注入你配置的

    public OpController(WebClient opClient) {
        this.opClient = opClient;
    }

    // 获取公共文章
    @RequestMapping("/article")
    public Result<PageVO<OpTag>> article() {
        log.info("获取公共文章");
        // 获取分类
        Result<List<OpTag>> result = opClient.get()
                .uri("/article/categories")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<List<OpTag>>>() {
                })
                .block();

        // 提取data
        if (result == null) {
            throw new BaseException("获取分类失败");
        }

        if (result.getCode() != 1) {
            throw new BaseException("获取分类失败:" + result.getMessage());
        }

        List<OpTag> tags = result.getData();

        log.info("分类: {}", JSON.toJSONString(tags, SerializerFeature.PrettyFormat));

        // 获取“blog”分类
        OpTag blogTag = tags.stream().filter(tag -> tag.getName().equals("blog")).findFirst().orElse(null);
        if (blogTag == null) {
            throw new BaseException("获取分类失败: blog分类不存在");
        }

        // 获取“blog”分类的文章
        OpArticleQuery query = new OpArticleQuery();
        query.setTagId(blogTag.getId());
        PageDTO<OpArticleQuery> dto = new PageDTO<>();
        dto.setQuery(query);
        dto.setPageNum(1);
        dto.setPageSize(100000);

        log.info("查询参数: {}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));

        Result<PageVO<OpArticle>> articleResult = opClient.post()
                .uri("/article/articles/public/page")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<PageVO<OpArticle>>>() {
                })
                .block();

        // 提取data
        if (articleResult == null) {
            throw new BaseException("获取文章失败");
        }

        if (articleResult.getCode() != 1) {
            throw new BaseException("获取文章失败:" + articleResult.getMessage());
        }

        List<OpArticle> articles = articleResult.getData().getRows();

        log.info("文章: {}", JSON.toJSONString(articles, SerializerFeature.PrettyFormat));

        // 将articles的分类id中去除 blog
        articles.forEach(article -> {
            article.getTagIds().removeIf(tagId -> tagId.equals(blogTag.getId()));
        });

        // 收集文章到分类Tag，没有分类就收集到blog
        articles.forEach(article -> {
            if (article.getTagIds().isEmpty()) {
                blogTag.getArticles().add(article);
            } else {
                tags.stream().filter(tag -> tag.getId().equals(article.getTagIds().get(0))).findFirst().ifPresent(tag -> tag.getArticles().add(article));
            }
        });

        // 移除没有文章的分类
        tags.removeIf(tag -> tag.getArticles().isEmpty());

        return Result.success(new PageVO<>(articles.size(), tags));
    }

    // 获取音乐
    @RequestMapping("/music")
    public Result<OpMusic> music() {
        log.info("获取音乐");
        return opClient.get()
                .uri("/music/musics/random")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<OpMusic>>() {
                })
                .block();
    }

    // page音乐
    @RequestMapping("/music/page")
    public Result<PageVO<OpMusic>> musicPage() {
        log.info("获取音乐");
        PageDTO<OpMusic> query = new PageDTO<>();
        query.setPageNum(1);
        query.setPageSize(100000);
        query.setQuery(new OpMusic());
        query.getQuery().setFavorite(true);
        Result<PageVO<OpMusic>> block = opClient.post()
                .uri("/music/musics/page")
                .bodyValue(query)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<PageVO<OpMusic>>>() {
                })
                .block();

        if (block != null) {
            return Result.success(new PageVO<>(block.getData().getTotal(), block.getData().getRows()));
        } else {
            return Result.error("获取音乐失败");
        }
    }
}
