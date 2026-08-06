package com.besenior.harucoding.global.config;

import com.besenior.harucoding.global.jwt.JwtFilter;
import com.besenior.harucoding.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요
                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/google",
                                "/api/auth/refresh",
                                // 로그아웃은 access token이 만료된 뒤에도 서버 세션을 정리할 수
                                // 있어야 하므로 통과시키고, 토큰 검증은 AuthService가 직접 한다.
                                "/api/auth/logout",
                                "/api/topics",
                                "/api/problem-sets/today",
                                "/v1/nesting/**",
                                // Spring이 처리하지 못한 예외를 /error 로 포워드할 때 인증에
                                // 걸려 400/500이 403으로 둔갑하던 문제를 막는다.
                                "/error"
                                // /api/recommend/** 는 인증 필요로 변경.
                                // 공개 상태에서는 바디의 userId만 믿고 온보딩 결과를 저장했기 때문에
                                // 남의 userId를 넣어 온보딩 정보를 덮어쓸 수 있었다.
                        ).permitAll()
                        // 문제 조회(GET)는 공개, 풀이 제출(POST)은 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/problems", "/api/problems/**").permitAll()
                        // 나머지 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}