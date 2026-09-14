package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.dto.OpMusic;
import blog.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Slf4j
@RequestMapping("/api/op")
@RestController
public class OpController {
    private final WebClient opClient;

    public OpController(WebClient opClient) {
        this.opClient = opClient;
    }

    // 获取音乐
    @RequestMapping("/music")
    public Result<OpMusic> music() {
        log.info("获取音乐");
        try {
            return opClient.get()
                    .uri("/music/musics/random")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<OpMusic>>() {
                    })
                    .block();
        } catch (WebClientRequestException e) {
            log.warn("Op 服务连接失败", e);
            return Result.error("Op 服务未启动");
        }
    }

    // page音乐
    @RequestMapping("/music/page")
    public Result<PageVO<OpMusic>> musicPage() {
        log.info("获取音乐");
        try {
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
        } catch (WebClientRequestException e) {
            log.warn("Op 服务连接失败", e);
            return Result.error("Op 服务未启动");
        }
    }
}
