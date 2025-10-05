package com.ayungi.cms.dto.response;

import com.ayungi.cms.entity.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о логе аудита
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private UUID id;
    
    private UUID userId;
    
    private String username;
    
    private AuditAction action;
    
    private String entityType;
    
    private String entityId;
    
    private String details;
    
    private String ipAddress;
    
    private LocalDateTime timestamp;
}
