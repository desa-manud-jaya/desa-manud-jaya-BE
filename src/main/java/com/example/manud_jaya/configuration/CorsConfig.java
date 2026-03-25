package com.example.manud_jaya.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    };

    private static final String[] ALLOWED_HEADERS = {
            "Content-Type", "Authorization", "X-Requested-With", "Accept"
    };

    @Bean
    @Profile("dev")
    public WebMvcConfigurer corsConfigurerDev() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods(ALLOWED_METHODS)
                        .allowedHeaders(ALLOWED_HEADERS)
                        .exposedHeaders("Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    @Profile("prod")
    public WebMvcConfigurer corsConfigurerProd() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://desa-manudjaya.vercel.app")
                        .allowedMethods(ALLOWED_METHODS)
                        .allowedHeaders(ALLOWED_HEADERS)
                        .exposedHeaders("Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    @Profile({"local", "default"})
    public WebMvcConfigurer corsConfigurerLocal() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:3001",
                                "http://localhost:5173"
                        )
                        .allowedMethods(ALLOWED_METHODS)
                        .allowedHeaders(ALLOWED_HEADERS)
                        .exposedHeaders("Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
