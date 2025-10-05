package com.ayungi.cms.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для перевода средств между картами
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @NotNull(message = "ID карты-источника не может быть пустым")
    private UUID fromCardId;

    @NotNull(message = "ID карты-получателя не может быть пустым")
    private UUID toCardId;

    @NotNull(message = "Сумма перевода не может быть пустой")
    @DecimalMin(value = "0.01", inclusive = true, message = "Сумма перевода должна быть больше 0")
    @Digits(integer = 17, fraction = 2, message = "Некорректный формат суммы")
    private BigDecimal amount;
}
