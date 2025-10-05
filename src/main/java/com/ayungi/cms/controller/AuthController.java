package com.ayungi.cms.controller;

import com.ayungi.cms.dto.request.LoginRequest;
import com.ayungi.cms.dto.request.RegisterRequest;
import com.ayungi.cms.dto.response.AuthResponse;
import com.ayungi.cms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для аутентификации и регистрации
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Аутентификация и регистрация пользователей")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создание нового аккаунта пользователя")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Запрос на регистрацию пользователя: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Авторизация пользователя
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентификация пользователя и получение JWT токенов")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("Запрос на вход пользователя: {}", request.getUsername());
        String ipAddress = httpRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }

    /**
     * Обновление access токена
     */
    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена", description = "Получение нового access токена через refresh токен")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("Запрос на обновление токена");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}
