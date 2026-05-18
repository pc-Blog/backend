package blog.controller;

import blog.common.Result;
import blog.service.UserService;
import blog.util.JwtUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final String clientId;
    private final String redirectUri;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          @Value("${github.client-id}") String clientId,
                          @Value("${github.redirect-uri}") String redirectUri) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        log.info("用户登录, username:{}", body.get("username"));
        Map<String, Object> result = userService.login(body.get("username"), body.get("password"));
        return Result.success(result);
    }

    @GetMapping("/github")
    public Result<String> githubLogin() {
        String url = "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=user:email";
        log.info("GitHub OAuth 重定向: {}", url);
        return Result.success(url);
    }

    @GetMapping("/github/callback")
    public Result<Map<String, Object>> githubCallback(@RequestParam String code) {
        log.info("GitHub OAuth 回调, code:{}", code);
        Map<String, Object> result = userService.loginByGithub(code);
        log.info("GitHub 登录成功:{}", JSON.toJSONString(result.get("user"), SerializerFeature.PrettyFormat));
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<Long> me(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long userId = jwtUtil.getUserId(header.substring(7));
            log.info("获取当前用户, userId:{}", userId);
            return Result.success(userId);
        }
        return Result.error("未登录");
    }
}
