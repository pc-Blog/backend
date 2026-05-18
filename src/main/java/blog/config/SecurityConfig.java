package blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ===== 需要登录 =====
                // 评论: 发表/编辑/删除
                .requestMatchers(HttpMethod.POST, "/api/comment").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/comment/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/comment/**").authenticated()
                // 文章管理
                .requestMatchers(HttpMethod.POST, "/api/article").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/article").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/article/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/article/*/publish").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/article/*/unpublish").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/article/*/pin").authenticated()
                // 项目管理
                .requestMatchers(HttpMethod.POST, "/api/project").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/project").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/project/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/project/*/publish").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/project/*/unpublish").authenticated()
                // 分类管理
                .requestMatchers(HttpMethod.POST, "/api/category").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/category").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/category/**").authenticated()
                // 标签管理
                .requestMatchers(HttpMethod.POST, "/api/tag").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/tag").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/tag/**").authenticated()
                // 时间线管理
                .requestMatchers(HttpMethod.POST, "/api/timeline").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/timeline").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/timeline/**").authenticated()
                // 技能管理
                .requestMatchers(HttpMethod.POST, "/api/skill").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/skill").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/skill/**").authenticated()
                // 关于管理
                .requestMatchers(HttpMethod.PUT, "/api/about").authenticated()
                // 媒体管理
                .requestMatchers(HttpMethod.POST, "/api/media/upload").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/media").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/media/**").authenticated()
                // 用户管理
                .requestMatchers(HttpMethod.PUT, "/api/user").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user/**").authenticated()
                // ===== 其他全部公开 =====
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
