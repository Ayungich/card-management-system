package com.ayungi.cms.util;

import com.ayungi.cms.entity.Card;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.CardStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CardValidator
 */
@ExtendWith(MockitoExtension.class)
class CardValidatorTest {

    @InjectMocks
    private CardValidator cardValidator;

    @Test
    void isCardUsable_WithActiveAndNotExpired_ShouldReturnTrue() {
        // Given
        Card card = Card.builder()
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .build();

        // When
        boolean isUsable = cardValidator.isCardUsable(card);

        // Then
        assertTrue(isUsable);
    }

    @Test
    void isCardUsable_WithBlockedCard_ShouldReturnFalse() {
        // Given
        Card card = Card.builder()
                .status(CardStatus.BLOCKED)
                .expirationDate(LocalDate.now().plusYears(1))
                .build();

        // When
        boolean isUsable = cardValidator.isCardUsable(card);

        // Then
        assertFalse(isUsable);
    }

    @Test
    void isCardUsable_WithExpiredCard_ShouldReturnFalse() {
        // Given
        Card card = Card.builder()
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().minusDays(1))
                .build();

        // When
        boolean isUsable = cardValidator.isCardUsable(card);

        // Then
        assertFalse(isUsable);
    }

    @Test
    void hasSufficientBalance_WithEnoughBalance_ShouldReturnTrue() {
        // Given
        Card card = Card.builder()
                .balance(new BigDecimal("1000.00"))
                .build();
        BigDecimal amount = new BigDecimal("500.00");

        // When
        boolean hasSufficient = cardValidator.hasSufficientBalance(card, amount);

        // Then
        assertTrue(hasSufficient);
    }

    @Test
    void hasSufficientBalance_WithInsufficientBalance_ShouldReturnFalse() {
        // Given
        Card card = Card.builder()
                .balance(new BigDecimal("100.00"))
                .build();
        BigDecimal amount = new BigDecimal("500.00");

        // When
        boolean hasSufficient = cardValidator.hasSufficientBalance(card, amount);

        // Then
        assertFalse(hasSufficient);
    }

    @Test
    void isSameOwner_WithSameOwner_ShouldReturnTrue() {
        // Given
        UUID ownerId = UUID.randomUUID();
        User owner = User.builder().id(ownerId).build();
        
        Card card1 = Card.builder().owner(owner).build();
        Card card2 = Card.builder().owner(owner).build();

        // When
        boolean isSame = cardValidator.isSameOwner(card1, card2);

        // Then
        assertTrue(isSame);
    }

    @Test
    void isSameOwner_WithDifferentOwners_ShouldReturnFalse() {
        // Given
        User owner1 = User.builder().id(UUID.randomUUID()).build();
        User owner2 = User.builder().id(UUID.randomUUID()).build();
        
        Card card1 = Card.builder().owner(owner1).build();
        Card card2 = Card.builder().owner(owner2).build();

        // When
        boolean isSame = cardValidator.isSameOwner(card1, card2);

        // Then
        assertFalse(isSame);
    }

    @Test
    void validateTransfer_WithValidTransfer_ShouldReturnNull() {
        // Given
        UUID ownerId = UUID.randomUUID();
        User owner = User.builder().id(ownerId).build();
        
        Card fromCard = Card.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .build();
                
        Card toCard = Card.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .build();
                
        BigDecimal amount = new BigDecimal("500.00");

        // When
        String error = cardValidator.validateTransfer(fromCard, toCard, amount);

        // Then
        assertNull(error);
    }

    @Test
    void validateTransfer_WithSameCard_ShouldReturnError() {
        // Given
        UUID cardId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).build();
        
        Card card = Card.builder()
                .id(cardId)
                .owner(owner)
                .status(CardStatus.ACTIVE)
                .expirationDate(LocalDate.now().plusYears(1))
                .balance(new BigDecimal("1000.00"))
                .build();
                
        BigDecimal amount = new BigDecimal("500.00");

        // When
        String error = cardValidator.validateTransfer(card, card, amount);

        // Then
        assertNotNull(error);
        assertTrue(error.contains("ту же карту"));
    }
}
