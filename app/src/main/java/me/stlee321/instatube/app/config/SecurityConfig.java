package me.stlee321.instatube.app.config;

import me.stlee321.instatube.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "me.stlee321.instatube.jwt")
public class SecurityConfig {
    JwtTokenFilter jwtTokenFilter;
    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .sessionManagement((sm) -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((req) -> {
                    req
                            .requestMatchers(HttpMethod.POST, "/api/auth/signout").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/auth/pwd/change").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/user/me").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/user/me").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/post/following").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/post").authenticated()
                            .requestMatchers(HttpMethod.PATCH, "/api/post/p/*").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/post/p/*").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/reply").authenticated()
                            .requestMatchers(HttpMethod.PATCH, "/api/reply/*").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/reply/*").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/img/post").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/img/avatar/setme").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/like/p/*").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/like/p/*").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/like/r/*").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/like/r/*").authenticated()
                            .requestMatchers(HttpMethod.POST, "/api/follow/*").authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/follow/*").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/is/following").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/is/following/*").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/noti/sse").authenticated()
                            .requestMatchers(HttpMethod.GET, "/api/noti").authenticated()
                            .anyRequest().permitAll();
                });
        return http.build();
    }
}
