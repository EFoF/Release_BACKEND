package com.service.releasenote.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.service.releasenote.global.jwt.JwtFilter.AUTHORIZATION_HEADER;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://10.0.16.169:80") // ILB 추가
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "HEAD", "OPTIONS")
                .allowCredentials(true) // 인증 정보(쿠키, 인증 헤더 등)를 전송할 수 있도록 허용
                .maxAge(3600) // Preflight 요청의 캐싱 시간을 설정, 1시간
                .exposedHeaders(AUTHORIZATION_HEADER); // Header 에서 Authorization 받을 수 있도록 설정
    }
}
