package com.ayungi.cms.dto.response;

import com.ayungi.cms.entity.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для ответа с информацией о транзакции
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private UUID id;
    
    private UUID fromCardId;
    
    private String fromCardMaskedNumber;
    
    private UUID toCardId;
    
    private String toCardMaskedNumber;
    
    private BigDecimal amount;
    
    private TransactionStatus status;
    
    private String failureReason;
    
    private LocalDateTime timestamp;
}
