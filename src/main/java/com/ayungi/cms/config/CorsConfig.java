package com.ayungi.cms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing)
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Разрешенные источники (в production указать конкретные домены)
        config.setAllowedOriginPatterns(List.of("*"));
        
        // Разрешенные HTTP методы
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Разрешенные заголовки
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Refresh-Token"
        ));
        
        // Заголовки, которые можно отправить клиенту
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Refresh-Token"
        ));
        
        // Разрешить отправку cookies
        config.setAllowCredentials(true);
        
        // Время кеширования preflight запросов (в секундах)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
