package com.ayungi.cms.service;

import com.ayungi.cms.dto.mapper.UserMapper;
import com.ayungi.cms.dto.request.UpdateUserRequest;
import com.ayungi.cms.dto.response.UserResponse;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.AuditAction;
import com.ayungi.cms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для управления пользователями
 */
@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    /**
     * Получение всех пользователей
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Получение всех пользователей");
        
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Получение пользователя по ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        log.debug("Получение пользователя по ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        return userMapper.toResponse(user);
    }

    /**
     * Получение пользователя по username
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Получение пользователя по username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        return userMapper.toResponse(user);
    }

    /**
     * Обновление данных пользователя
     */
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, User currentUser) {
        log.info("Обновление пользователя: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка прав (пользователь может обновлять только свои данные)
        if (!user.getId().equals(currentUser.getId()) && 
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            throw new RuntimeException("Недостаточно прав для обновления данных пользователя");
        }

        boolean updated = false;

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email уже используется");
            }
            user.setEmail(request.getEmail());
            updated = true;
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
            log.info("Пользователь {} успешно обновлен", user.getUsername());
            
            auditService.logAction(currentUser, AuditAction.UPDATE, "User", user.getId().toString(),
                    "Обновление данных пользователя", null);
        }

        return userMapper.toResponse(user);
    }

    /**
     * Удаление пользователя (только для ADMIN)
     */
    @Transactional
    public void deleteUser(UUID userId, User currentUser) {
        log.info("Удаление пользователя: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Нельзя удалить самого себя
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нельзя удалить собственный аккаунт");
        }

        userRepository.delete(user);
        log.info("Пользователь {} успешно удален", user.getUsername());
        
        auditService.logAction(currentUser, AuditAction.DELETE, "User", user.getId().toString(),
                "Удаление пользователя: " + user.getUsername(), null);
    }

    /**
     * Поиск пользователей
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Поиск пользователей: {}", searchTerm);
        
        return userRepository.searchUsers(searchTerm, pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Получение активных пользователей
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        log.debug("Получение активных пользователей");
        
        return userRepository.findByEnabledTrue(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Получение пользователей по роли
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        log.debug("Получение пользователей с ролью: {}", roleName);
        
        return userRepository.findByRoleName(roleName, pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Блокировка/разблокировка пользователя
     */
    @Transactional
    public UserResponse toggleUserStatus(UUID userId, User currentUser) {
        log.info("Изменение статуса пользователя: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Нельзя заблокировать самого себя
        if (user.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Нельзя изменить статус собственного аккаунта");
        }

        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);
        
        String action = user.getEnabled() ? "Разблокировка" : "Блокировка";
        log.info("{} пользователя {}", action, user.getUsername());
        
        auditService.logAction(currentUser, AuditAction.UPDATE, "User", user.getId().toString(),
                action + " пользователя", null);

        return userMapper.toResponse(user);
    }
}
