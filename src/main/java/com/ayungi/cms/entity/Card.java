package com.ayungi.cms.entity;

import com.ayungi.cms.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность банковской карты
 */
@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_owner_id", columnList = "owner_id"),
        @Index(name = "idx_card_status", columnList = "status"),
        @Index(name = "idx_card_number", columnList = "card_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Номер карты (зашифрованный)
     */
    @Column(name = "card_number", nullable = false, unique = true, length = 500)
    private String cardNumber;

    /**
     * Владелец карты
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Срок действия карты
     */
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    /**
     * Статус карты (ACTIVE, BLOCKED, EXPIRED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CardStatus status = CardStatus.ACTIVE;

    /**
     * Текущий баланс карты
     */
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Транзакции, где карта является источником
     */
    @OneToMany(mappedBy = "fromCard", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    /**
     * Транзакции, где карта является получателем
     */
    @OneToMany(mappedBy = "toCard", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> incomingTransactions = new HashSet<>();

    /**
     * Дата выпуска карты
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего изменения
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Проверка, истек ли срок действия карты
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    /**
     * Проверка, активна ли карта
     */
    public boolean isActive() {
        return status == CardStatus.ACTIVE && !isExpired();
    }

    /**
     * Проверка, заблокирована ли карта
     */
    public boolean isBlocked() {
        return status == CardStatus.BLOCKED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return id != null && id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
