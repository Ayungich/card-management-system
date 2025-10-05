package com.ayungi.cms.util;

import com.ayungi.cms.entity.Card;
import com.ayungi.cms.entity.enums.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Утилита для валидации банковских карт
 */
@Component
@Slf4j
public class CardValidator {

    /**
     * Проверка, может ли карта использоваться для операций
     *
     * @param card карта для проверки
     * @return true если карта активна и не истекла
     */
    public boolean isCardUsable(Card card) {
        if (card == null) {
            log.warn("Попытка валидации null карты");
            return false;
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.debug("Карта {} не активна, статус: {}", card.getId(), card.getStatus());
            return false;
        }

        if (isExpired(card)) {
            log.debug("Карта {} истекла", card.getId());
            return false;
        }

        return true;
    }

    /**
     * Проверка истечения срока действия карты
     *
     * @param card карта для проверки
     * @return true если срок действия истек
     */
    public boolean isExpired(Card card) {
        if (card == null || card.getExpirationDate() == null) {
            return true;
        }

        return LocalDate.now().isAfter(card.getExpirationDate());
    }

    /**
     * Проверка достаточности средств на карте
     *
     * @param card карта для проверки
     * @param amount требуемая сумма
     * @return true если средств достаточно
     */
    public boolean hasSufficientBalance(Card card, BigDecimal amount) {
        if (card == null || amount == null) {
            return false;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Попытка проверки с некорректной суммой: {}", amount);
            return false;
        }

        return card.getBalance().compareTo(amount) >= 0;
    }

    /**
     * Проверка возможности выполнения перевода
     *
     * @param fromCard карта-источник
     * @param toCard карта-получатель
     * @param amount сумма перевода
     * @return сообщение об ошибке или null если все в порядке
     */
    public String validateTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard == null) {
            return "Карта-источник не найдена";
        }

        if (toCard == null) {
            return "Карта-получатель не найдена";
        }

        if (fromCard.getId().equals(toCard.getId())) {
            return "Нельзя переводить средства на ту же карту";
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Сумма перевода должна быть больше нуля";
        }

        if (!isCardUsable(fromCard)) {
            return "Карта-источник не может использоваться для операций";
        }

        if (!isCardUsable(toCard)) {
            return "Карта-получатель не может использоваться для операций";
        }

        if (!hasSufficientBalance(fromCard, amount)) {
            return "Недостаточно средств на карте-источнике";
        }

        return null; // Все проверки пройдены
    }

    /**
     * Проверка, принадлежат ли обе карты одному владельцу
     *
     * @param fromCard первая карта
     * @param toCard вторая карта
     * @return true если владелец один
     */
    public boolean isSameOwner(Card fromCard, Card toCard) {
        if (fromCard == null || toCard == null) {
            return false;
        }

        if (fromCard.getOwner() == null || toCard.getOwner() == null) {
            return false;
        }

        return fromCard.getOwner().getId().equals(toCard.getOwner().getId());
    }

    /**
     * Проверка лимита баланса карты
     *
     * @param currentBalance текущий баланс
     * @param amountToAdd сумма для добавления
     * @param maxBalance максимальный баланс
     * @return true если не превышен лимит
     */
    public boolean isWithinBalanceLimit(BigDecimal currentBalance, BigDecimal amountToAdd, BigDecimal maxBalance) {
        if (currentBalance == null || amountToAdd == null || maxBalance == null) {
            return false;
        }

        BigDecimal newBalance = currentBalance.add(amountToAdd);
        return newBalance.compareTo(maxBalance) <= 0;
    }

    /**
     * Проверка, заблокирована ли карта
     *
     * @param card карта для проверки
     * @return true если карта заблокирована
     */
    public boolean isBlocked(Card card) {
        if (card == null) {
            return true;
        }

        return card.getStatus() == CardStatus.BLOCKED;
    }

    /**
     * Проверка срока действия карты (осталось менее N дней)
     *
     * @param card карта для проверки
     * @param daysThreshold количество дней
     * @return true если срок истекает скоро
     */
    public boolean isExpiringSoon(Card card, int daysThreshold) {
        if (card == null || card.getExpirationDate() == null) {
            return false;
        }

        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return card.getExpirationDate().isBefore(thresholdDate) && 
               !card.getExpirationDate().isBefore(LocalDate.now());
    }
}
