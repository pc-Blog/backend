package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.FriendLink;
import blog.service.FriendLinkService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/friend-link")
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    public FriendLinkController(FriendLinkService friendLinkService) {
        this.friendLinkService = friendLinkService;
    }

    @GetMapping("/list")
    public Result<List<FriendLink>> getPublishedList() {
        log.info("查询已发布的友链列表");
        return Result.success(friendLinkService.getPublishedList());
    }

    @GetMapping("/{id}")
    public Result<FriendLink> getById(@PathVariable Long id) {
        log.info("根据ID查询友链, id:{}", id);
        return Result.success(friendLinkService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody FriendLink friendLink) {
        log.info("新增友链:{}", JSON.toJSONString(friendLink, SerializerFeature.PrettyFormat));
        friendLinkService.save(friendLink);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody FriendLink friendLink) {
        log.info("更新友链:{}", JSON.toJSONString(friendLink, SerializerFeature.PrettyFormat));
        friendLinkService.updateById(friendLink);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除友链, id:{}", id);
        friendLinkService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<FriendLink>> page(@RequestBody PageDTO<FriendLink> dto) {
        log.info("分页查询友链:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(friendLinkService.page(dto));
    }
}
