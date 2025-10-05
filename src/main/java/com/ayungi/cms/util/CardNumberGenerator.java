package com.ayungi.cms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Утилита для генерации номеров банковских карт (алгоритм Луна)
 */
@Component
@Slf4j
public class CardNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DEFAULT_BIN = "4276"; // Visa

    /**
     * Генерация валидного номера карты (16 цифр)
     *
     * @return номер карты
     */
    public String generateCardNumber() {
        return generateCardNumber(DEFAULT_BIN);
    }

    /**
     * Генерация валидного номера карты с указанным BIN
     *
     * @param bin первые цифры номера карты (BIN - Bank Identification Number)
     * @return номер карты
     */
    public String generateCardNumber(String bin) {
        if (bin == null || bin.length() > 15) {
            bin = DEFAULT_BIN;
        }

        // Генерируем случайные цифры для заполнения до 15 символов
        StringBuilder cardNumber = new StringBuilder(bin);
        int remainingDigits = 15 - bin.length();

        for (int i = 0; i < remainingDigits; i++) {
            cardNumber.append(RANDOM.nextInt(10));
        }

        // Добавляем контрольную цифру по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        log.debug("Сгенерирован номер карты: {}", maskForLog(cardNumber.toString()));
        return cardNumber.toString();
    }

    /**
     * Вычисление контрольной цифры по алгоритму Луна
     *
     * @param cardNumber номер карты без контрольной цифры (15 цифр)
     * @return контрольная цифра
     */
    private int calculateLuhnCheckDigit(String cardNumber) {
        int sum = 0;
        boolean alternate = true;

        // Проходим по цифрам справа налево
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    /**
     * Валидация номера карты по алгоритму Луна
     *
     * @param cardNumber номер карты
     * @return true если номер валиден
     */
    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        // Убираем все нецифровые символы
        String digitsOnly = cardNumber.replaceAll("\\D", "");

        if (digitsOnly.length() != 16) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        // Проходим по цифрам справа налево
        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digitsOnly.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }

    /**
     * Маскирование номера карты для логирования
     */
    private String maskForLog(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
