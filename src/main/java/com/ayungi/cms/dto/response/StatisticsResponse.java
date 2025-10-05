package com.ayungi.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для ответа со статистикой системы (для админа)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {

    private Long totalUsers;
    
    private Long totalCards;
    
    private Long activeCards;
    
    private Long blockedCards;
    
    private Long expiredCards;
    
    private Long totalTransactions;
    
    private Long successfulTransactions;
    
    private Long failedTransactions;
    
    private BigDecimal totalBalance;
    
    private BigDecimal totalTransactionAmount;
}
