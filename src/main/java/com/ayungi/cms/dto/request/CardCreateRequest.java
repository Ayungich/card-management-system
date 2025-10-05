package com.ayungi.cms.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для создания новой карты (только для ADMIN)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCreateRequest {

    @NotNull(message = "ID владельца не может быть пустым")
    private UUID ownerId;

    @NotNull(message = "Срок действия карты не может быть пустым")
    @Future(message = "Срок действия карты должен быть в будущем")
    private LocalDate expirationDate;

    @NotNull(message = "Начальный баланс не может быть пустым")
    @DecimalMin(value = "0.0", inclusive = true, message = "Начальный баланс не может быть отрицательным")
    @Digits(integer = 17, fraction = 2, message = "Некорректный формат баланса")
    private BigDecimal initialBalance;
}
