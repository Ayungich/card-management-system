package com.ayungi.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа с балансом карты
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private UUID cardId;
    
    private String maskedCardNumber;
    
    private BigDecimal balance;
    
    @Builder.Default
    private String currency = "RUB";
}
