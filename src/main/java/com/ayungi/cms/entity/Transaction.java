package com.ayungi.cms.entity;

import com.ayungi.cms.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность транзакции (перевода между картами)
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_from_card", columnList = "from_card_id"),
        @Index(name = "idx_transaction_to_card", columnList = "to_card_id"),
        @Index(name = "idx_transaction_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Карта-источник (с которой переводят)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    /**
     * Карта-получатель (на которую переводят)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    /**
     * Сумма перевода
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Статус транзакции (SUCCESS, FAILED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    /**
     * Причина неудачи (если status = FAILED)
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Время выполнения транзакции
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction transaction)) return false;
        return id != null && id.equals(transaction.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
