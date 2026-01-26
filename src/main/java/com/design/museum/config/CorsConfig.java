package com.design.museum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("http://localhost:3000", "http://localhost:8081") // 允许的前端地址
                        .allowedMethods("*")   // GET, POST, PUT, DELETE 等
                        .allowedHeaders("*")   // 允许任何头部
                        .allowCredentials(false) // 允许携带 Cookie
                        //TODO 记得改为true
                        .maxAge(3600);
            }
        };
    }
}
