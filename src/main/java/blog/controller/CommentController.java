package blog.controller;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.common.Result;
import blog.entity.Comment;
import blog.entity.User;
import blog.service.CommentService;
import blog.service.UserService;
import blog.util.JwtUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CommentController(CommentService commentService, JwtUtil jwtUtil, UserService userService) {
        this.commentService = commentService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<Comment> getById(@PathVariable Long id) {
        log.info("根据ID查询评论, id:{}", id);
        return Result.success(commentService.getById(id));
    }

    @PostMapping
    public Result<Comment> save(@Valid @RequestBody Comment comment, HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userService.getById(userId);
        comment.setUserId(userId);
        comment.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        comment.setAuthorEmail(user.getEmail());
        log.info("新增评论, userId:{}: {}", userId, JSON.toJSONString(comment, SerializerFeature.PrettyFormat));
        commentService.save(comment);
        return Result.success(comment);
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody Comment comment) {
        log.info("更新评论:{}", JSON.toJSONString(comment, SerializerFeature.PrettyFormat));
        commentService.updateById(comment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除评论, id:{}", id);
        commentService.removeById(id);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageVO<Comment>> page(@RequestBody PageDTO<Comment> dto) {
        log.info("分页查询评论:{}", JSON.toJSONString(dto, SerializerFeature.PrettyFormat));
        return Result.success(commentService.page(dto));
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return jwtUtil.getUserId(header.substring(7));
        }
        throw new RuntimeException("未登录");
    }
}
