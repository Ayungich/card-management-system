package com.ayungi.cms.dto.mapper;

import com.ayungi.cms.dto.response.AuditLogResponse;
import com.ayungi.cms.entity.AuditLog;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования AuditLog в DTO
 */
@Component
public class AuditLogMapper {

    /**
     * Преобразование AuditLog в AuditLogResponse
     */
    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .username(auditLog.getUser() != null ? auditLog.getUser().getUsername() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
