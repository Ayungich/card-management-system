package com.ayungi.cms.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для CardNumberGenerator
 */
@ExtendWith(MockitoExtension.class)
class CardNumberGeneratorTest {

    @InjectMocks
    private CardNumberGenerator cardNumberGenerator;

    @Test
    void generateCardNumber_ShouldReturnValidCardNumber() {
        // When
        String cardNumber = cardNumberGenerator.generateCardNumber();

        // Then
        assertNotNull(cardNumber);
        assertEquals(16, cardNumber.length());
        assertTrue(cardNumber.matches("\\d{16}"));
    }

    @Test
    void generateCardNumber_ShouldPassLuhnValidation() {
        // When
        String cardNumber = cardNumberGenerator.generateCardNumber();

        // Then
        assertTrue(cardNumberGenerator.validateCardNumber(cardNumber));
    }

    @Test
    void validateCardNumber_WithValidNumber_ShouldReturnTrue() {
        // Given
        String validCardNumber = "4276123456789012"; // Valid Luhn

        // When
        boolean isValid = cardNumberGenerator.validateCardNumber(validCardNumber);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateCardNumber_WithInvalidNumber_ShouldReturnFalse() {
        // Given
        String invalidCardNumber = "1234567890123456";

        // When
        boolean isValid = cardNumberGenerator.validateCardNumber(invalidCardNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateCardNumber_WithNullOrEmpty_ShouldReturnFalse() {
        // When & Then
        assertFalse(cardNumberGenerator.validateCardNumber(null));
        assertFalse(cardNumberGenerator.validateCardNumber(""));
    }

    @Test
    void generateCardNumber_WithCustomBIN_ShouldStartWithBIN() {
        // Given
        String customBIN = "5555";

        // When
        String cardNumber = cardNumberGenerator.generateCardNumber(customBIN);

        // Then
        assertTrue(cardNumber.startsWith(customBIN));
        assertEquals(16, cardNumber.length());
    }
}
