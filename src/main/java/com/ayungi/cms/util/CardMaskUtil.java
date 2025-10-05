package com.ayungi.cms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Утилита для маскирования номеров карт
 */
@Component
@Slf4j
public class CardMaskUtil {

    private final EncryptionUtil encryptionUtil;

    public CardMaskUtil(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }

    /**
     * Маскирование номера карты (**** **** **** 1234)
     *
     * @param encryptedCardNumber зашифрованный номер карты
     * @return маскированный номер карты
     */
    public String maskCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.isEmpty()) {
            return "****";
        }

        try {
            // Дешифруем номер карты
            String decryptedNumber = encryptionUtil.decrypt(encryptedCardNumber);
            
            // Убираем все нецифровые символы
            String digitsOnly = decryptedNumber.replaceAll("\\D", "");
            
            if (digitsOnly.length() < 4) {
                return "****";
            }

            // Берем последние 4 цифры
            String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
            
            // Формируем маску
            return "**** **** **** " + lastFour;
        } catch (Exception e) {
            log.error("Ошибка маскирования номера карты", e);
            return "****";
        }
    }

    /**
     * Маскирование номера карты с показом первых 6 и последних 4 цифр (для админа)
     *
     * @param encryptedCardNumber зашифрованный номер карты
     * @return маскированный номер карты (1234 56** **** 7890)
     */
    public String maskCardNumberForAdmin(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.isEmpty()) {
            return "****";
        }

        try {
            String decryptedNumber = encryptionUtil.decrypt(encryptedCardNumber);
            String digitsOnly = decryptedNumber.replaceAll("\\D", "");
            
            if (digitsOnly.length() < 10) {
                return maskCardNumber(encryptedCardNumber);
            }

            String firstSix = digitsOnly.substring(0, 6);
            String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
            
            return String.format("%s %s** **** %s", 
                    firstSix.substring(0, 4), 
                    firstSix.substring(4, 6), 
                    lastFour);
        } catch (Exception e) {
            log.error("Ошибка маскирования номера карты для админа", e);
            return "****";
        }
    }

    /**
     * Форматирование номера карты (1234 5678 9012 3456)
     *
     * @param cardNumber номер карты
     * @return форматированный номер карты
     */
    public String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return cardNumber;
        }

        String digitsOnly = cardNumber.replaceAll("\\D", "");
        
        if (digitsOnly.length() != 16) {
            return cardNumber;
        }

        return String.format("%s %s %s %s",
                digitsOnly.substring(0, 4),
                digitsOnly.substring(4, 8),
                digitsOnly.substring(8, 12),
                digitsOnly.substring(12, 16));
    }
}
