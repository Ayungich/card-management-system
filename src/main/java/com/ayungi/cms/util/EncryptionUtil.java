package com.ayungi.cms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Утилита для шифрования и дешифрования номеров карт (AES-256)
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

    public EncryptionUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = generateKey(secret);
    }

    /**
     * Генерация ключа шифрования из секрета
     */
    private SecretKeySpec generateKey(String secret) {
        try {
            byte[] key = secret.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // AES-128 (16 bytes)
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            log.error("Ошибка генерации ключа шифрования", e);
            throw new RuntimeException("Не удалось сгенерировать ключ шифрования", e);
        }
    }

    /**
     * Шифрование строки (номера карты)
     *
     * @param plainText исходная строка
     * @return зашифрованная строка в Base64
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Ошибка шифрования данных", e);
            throw new RuntimeException("Не удалось зашифровать данные", e);
        }
    }

    /**
     * Дешифрование строки (номера карты)
     *
     * @param encryptedText зашифрованная строка в Base64
     * @return расшифрованная строка
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Ошибка дешифрования данных", e);
            throw new RuntimeException("Не удалось расшифровать данные", e);
        }
    }
}
