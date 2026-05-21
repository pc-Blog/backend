package blog.service.impl;

import blog.common.PageDTO;
import blog.common.PageVO;
import blog.entity.User;
import blog.exception.BaseException;
import blog.mapper.UserMapper;
import blog.service.UserService;
import blog.util.JwtUtil;
import blog.util.PageUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public UserServiceImpl(JwtUtil jwtUtil,
                           @Value("${github.client-id}") String clientId,
                           @Value("${github.client-secret}") String clientSecret,
                           @Value("${github.redirect-uri}") String redirectUri) {
        this.jwtUtil = jwtUtil;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public PageVO<User> page(PageDTO<User> dto) {
        var wrapper = new LambdaQueryWrapper<User>().eq(User::getDeleted, 0);
        User query = dto.getQuery();
        if (query != null && query.getUsername() != null && !query.getUsername().isBlank())
            wrapper.like(User::getUsername, query.getUsername());
        if (query != null && query.getNickname() != null && !query.getNickname().isBlank())
            wrapper.like(User::getNickname, query.getNickname());
        var page = PageUtil.<User>toPage(dto);
        page(page, wrapper);
        return new PageVO<>(page.getTotal(), page.getRecords());
    }

    @Override
    public boolean save(User user) {
        checkUsernameUnique(user.getUsername(), null);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        return super.save(user);
    }

    @Override
    public boolean updateById(User user) {
        checkUsernameUnique(user.getUsername(), user.getId());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        return super.updateById(user);
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new BaseException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", sanitize(user));
        return result;
    }

    @Override
    public Map<String, Object> loginByGithub(String code) {
        try {
            // 1. exchange code for access_token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            String body = "client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&code=" + code
                    + "&redirect_uri=" + redirectUri;

            var httpClient = createHttpClient();
            var tokenReq = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var tokenResp = httpClient.send(tokenReq, HttpResponse.BodyHandlers.ofString());
            JSONObject tokenJson = JSON.parseObject(tokenResp.body());
            String accessToken = tokenJson.getString("access_token");
            if (accessToken == null) {
                throw new BaseException("GitHub 授权失败");
            }

            // 2. get GitHub user info
            var userReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            var userResp = httpClient.send(userReq, HttpResponse.BodyHandlers.ofString());
            JSONObject ghUser = JSON.parseObject(userResp.body());
            String ghId = ghUser.getString("id");
            String ghLogin = ghUser.getString("login");
            String ghAvatar = ghUser.getString("avatar_url");
            String ghEmail = ghUser.getString("email");

            // 3. find or create user
            User user = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getGithubId, ghId)
                    .eq(User::getDeleted, 0));
            if (user == null) {
                user = new User();
                user.setUsername("gh_" + ghLogin);
                user.setNickname(ghLogin);
                user.setGithubId(ghId);
                user.setAvatar(ghAvatar);
                user.setEmail(ghEmail);
                user.setPassword("");
                save(user);
            } else {
                user.setAvatar(ghAvatar);
                user.setNickname(ghLogin);
                if (ghEmail != null) {
                    user.setEmail(ghEmail);
                }
                updateById(user);
            }

            // 4. generate JWT
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", sanitize(user));
            return result;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("GitHub 登录失败: " + e.getMessage());
        }
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> u = new HashMap<>();
        u.put("id", user.getId());
        u.put("username", user.getUsername());
        u.put("nickname", user.getNickname());
        u.put("avatar", user.getAvatar());
        u.put("email", user.getEmail());
        return u;
    }

    private static java.net.http.HttpClient createHttpClient() {
        try {
            var trustAll = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] c, String a) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] c, String a) {}
                }
            };
            var sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());
            return java.net.http.HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            throw new BaseException("SSL初始化失败: " + e.getMessage());
        }
    }

    private void checkUsernameUnique(String username, Long excludeId) {
        var wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BaseException("用户名已存在");
        }
    }
}
