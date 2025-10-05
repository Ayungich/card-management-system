package com.ayungi.cms.dto.response;

import com.ayungi.cms.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о карте
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponse {

    private UUID id;
    
    /**
     * Маскированный номер карты (**** **** **** 1234)
     */
    private String maskedCardNumber;
    
    private UUID ownerId;
    
    private String ownerUsername;
    
    private LocalDate expirationDate;
    
    private CardStatus status;
    
    private BigDecimal balance;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * Истек ли срок действия карты
     */
    private Boolean isExpired;
    
    /**
     * Активна ли карта
     */
    private Boolean isActive;
}
