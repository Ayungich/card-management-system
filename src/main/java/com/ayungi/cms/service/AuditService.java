package com.ayungi.cms.service;

import com.ayungi.cms.dto.mapper.AuditLogMapper;
import com.ayungi.cms.dto.response.AuditLogResponse;
import com.ayungi.cms.entity.AuditLog;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.AuditAction;
import com.ayungi.cms.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для работы с системным аудитом
 */
@Service
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditService(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * Логирование действия пользователя (асинхронно)
     */
    @Async
    @Transactional
    public void logAction(User user, AuditAction action, String entityType, 
                         String entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Создан лог аудита: {} для пользователя {}", action, user != null ? user.getUsername() : "system");
        } catch (Exception e) {
            log.error("Ошибка создания лога аудита", e);
        }
    }

    /**
     * Получение всех логов с фильтрацией
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogs(UUID userId, AuditAction action, String entityType,
                                             LocalDateTime startDate, LocalDateTime endDate,
                                             Pageable pageable) {
        log.debug("Получение логов аудита с фильтрами");
        
        return auditLogRepository.findAllWithFilters(userId, action, entityType, startDate, endDate, pageable)
                .map(auditLogMapper::toResponse);
    }

    /**
     * Получение логов пользователя
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserLogs(UUID userId, Pageable pageable) {
        log.debug("Получение логов пользователя: {}", userId);
        
        return auditLogRepository.findByUserId(userId, pageable)
                .map(auditLogMapper::toResponse);
    }

    /**
     * Получение истории входов пользователя
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLoginHistory(UUID userId, Pageable pageable) {
        log.debug("Получение истории входов пользователя: {}", userId);
        
        return auditLogRepository.findLoginHistoryByUserId(userId, pageable)
                .map(auditLogMapper::toResponse);
    }

    /**
     * Удаление старых логов (для очистки)
     */
    @Transactional
    public void deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        log.info("Удаление логов старше {}", cutoffDate);
        
        auditLogRepository.deleteOldLogs(cutoffDate);
    }
}
