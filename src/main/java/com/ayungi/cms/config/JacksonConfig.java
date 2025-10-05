package com.ayungi.cms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Конфигурация Jackson для сериализации/десериализации JSON
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Регистрация модуля для работы с Java 8 Date/Time API
        mapper.registerModule(new JavaTimeModule());
        
        // Отключение записи дат как timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Игнорирование неизвестных свойств при десериализации
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Форматирование JSON для читаемости (можно отключить в production)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
