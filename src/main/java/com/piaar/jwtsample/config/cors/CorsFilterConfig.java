package com.piaar.jwtsample.config.cors;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfig{
    @Bean
    public CorsFilter corsFilter(){
        // UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // CorsConfiguration config = new CorsConfiguration();
        // config.setAllowCredentials(true);
        // config.addAllowedOrigin("http://localhost:3000");
        // config.addAllowedHeader("*");
        // config.addAllowedMethod("*");
        // source.registerCorsConfiguration("/api/**", config);
        // return new CorsFilter(source);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // config.addAllowedOrigin("*");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:3001");
        config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.setExposedHeaders(Arrays.asList("Authorization"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
