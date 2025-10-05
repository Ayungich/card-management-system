package com.ayungi.cms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация Swagger/OpenAPI документации
 */
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.example.com")
                                .description("Production server")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Card Management System API")
                .description("REST API для управления банковскими картами. " +
                            "Система поддерживает создание карт, переводы между картами, " +
                            "управление пользователями и полный аудит операций.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Ayungich")
                        .email("support@example.com")
                        .url("https://github.com/ayungich"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Введите JWT токен, полученный при авторизации");
    }
}
