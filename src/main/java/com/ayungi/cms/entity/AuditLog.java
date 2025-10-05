package com.ayungi.cms.entity;

import com.ayungi.cms.entity.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для системного аудита операций
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Пользователь, выполнивший действие
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Тип действия (CREATE, UPDATE, DELETE, BLOCK, ACTIVATE, TRANSFER, LOGIN, LOGOUT)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    /**
     * Тип сущности, над которой выполнено действие (Card, User, Transaction)
     */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /**
     * ID сущности, над которой выполнено действие
     */
    @Column(name = "entity_id", length = 36)
    private String entityId;

    /**
     * Дополнительные детали операции (JSON или текст)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * IP-адрес, с которого выполнено действие
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Время выполнения действия
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog auditLog)) return false;
        return id != null && id.equals(auditLog.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
