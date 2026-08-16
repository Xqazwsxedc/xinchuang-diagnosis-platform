package com.xinchuang.diagnosis.config;

import com.xinchuang.diagnosis.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor jwtAuthInterceptor;

    public WebSecurityConfig(JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/auth/login",
                "/health",
                "/actuator/**"
            );
    }
}
