package com.ayungi.cms.exception;

import java.util.Map;

/**
 * Исключение при ошибках валидации
 */
public class ValidationException extends BaseException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.errors = null;
    }

    public ValidationException(String message, Map<String, String> errors) {
        super("VALIDATION_ERROR", message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
