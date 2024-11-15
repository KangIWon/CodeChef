package com.sparta.codechef.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Cors 문제로 만든 Config
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api로 시작하는 모든 경로에 대해 CORS 설정 적용
                .allowedOrigins("http://localhost:8080") // 허용할 도메인 (프론트엔드 서버 주소)
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메서드
                .allowedHeaders("Authorization", "Content-Type") // 요청에서 허용할 헤더
                .allowCredentials(true) // 쿠키, 세션 등의 인증 정보를 포함할지 여부
                .maxAge(3600); // 설정 캐시 지속 시간 (초 단위)
    }
}
