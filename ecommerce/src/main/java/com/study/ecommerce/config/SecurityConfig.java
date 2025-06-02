package com.study.ecommerce.config;

import com.study.ecommerce.global.security.JwtAuthenticationFilter;
import com.study.ecommerce.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //CSRF : 주로 JWT 같은 토큰 기반 인증을 사용할때 CSRF보호는 필요하지 않기 떄문에 끄기
                .csrf(AbstractHttpConfigurer::disable)

                //cors-다른 도메인에서 api 호출할 수 있도록 서버가 허용해주는 규칙
                //Spring Security에게 "CORS 설정을 수동으로 제공할게
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //서버가 세션을 유지 하지 않도록 설정합니다
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //인가 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**",  "/h2-console/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories", "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/seller/**").hasAnyRole("SELLER", "ADMIN")
                        .anyRequest().authenticated()
                )
                //로그인 폼 방식전에 JWT로 먼저 인증 수행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        //h2db를 손쉽게 사용하기 위함
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        //authorization: JWT 토큰 전송할때 사e용, content-type: JSON등 본문 데이터 형식 명시, x-auth-token:사용자 정의 헤더
        configuration.setAllowedHeaders(List.of("authorization", "content-type", "x-auth-token"));

        //서버 응답에서 브라우저가 접근 가능한 헤더 지정
        configuration.setExposedHeaders(List.of("x-auth-token"));

        //API 경로에 위 설정을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
