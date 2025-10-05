package com.ayungi.cms.exception;

import lombok.Getter;

/**
 * Базовое исключение для всех кастомных исключений приложения
 */
@Getter
public class BaseException extends RuntimeException {

    private final String code;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
