package com.ayungi.cms.service;

import com.ayungi.cms.dto.request.LoginRequest;
import com.ayungi.cms.dto.request.RegisterRequest;
import com.ayungi.cms.dto.response.AuthResponse;
import com.ayungi.cms.entity.Role;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.AuditAction;
import com.ayungi.cms.repository.RoleRepository;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для аутентификации и регистрации пользователей
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    /**
     * Регистрация нового пользователя
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Регистрация нового пользователя: {}", request.getUsername());

        // Проверка существования пользователя
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Попытка регистрации с существующим username: {}", request.getUsername());
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Попытка регистрации с существующим email: {}", request.getEmail());
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Получение роли USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));

        // Создание пользователя
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        user = userRepository.save(user);
        log.info("Пользователь {} успешно зарегистрирован", user.getUsername());

        // Аудит
        auditService.logAction(user, AuditAction.CREATE, "User", user.getId().toString(), 
                "Регистрация нового пользователя", null);

        // Генерация токенов
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Авторизация пользователя
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        log.info("Попытка входа пользователя: {}", request.getUsername());

        // Аутентификация
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Загрузка пользователя
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Генерация токенов
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        log.info("Пользователь {} успешно авторизован", user.getUsername());

        // Аудит
        auditService.logAction(user, AuditAction.LOGIN, null, null, 
                "Успешный вход в систему", ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Обновление access токена через refresh токен
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Обновление access токена");

        // Валидация refresh токена
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Невалидный refresh токен");
            throw new RuntimeException("Невалидный refresh токен");
        }

        // Извлечение username и генерация нового access токена
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
