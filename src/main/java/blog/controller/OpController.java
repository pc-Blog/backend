package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.OpArticleQuery;
import blog.dto.OpArticle;
import blog.dto.OpMusic;
import blog.dto.OpTag;
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

    // 获取分类
    @RequestMapping("/category")
    public Result<List<OpTag>> category() {
        log.info("获取分类");
        return opClient.get()
                .uri("/article/categories")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<List<OpTag>>>() {})
                .block();
    }

    // 获取公共文章
    @RequestMapping("/article")
    public Result<PageVO<OpArticle>> article(@RequestBody PageDTO<OpArticleQuery> query) {
        log.info("获取公共文章: {}", JSON.toJSONString(query, SerializerFeature.PrettyFormat));
        return opClient.post()
                .uri("/article/articles/public/page")
                .bodyValue(query)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<PageVO<OpArticle>>>() {})
                .block();
    }

    // 获取音乐
    @RequestMapping("/music")
    public Result<OpMusic> music() {
        log.info("获取音乐");
        return opClient.get()
                .uri("/music/musics/random")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<OpMusic>>() {})
                .block();
    }
}
