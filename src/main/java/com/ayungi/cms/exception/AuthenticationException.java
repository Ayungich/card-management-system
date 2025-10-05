package com.ayungi.cms.exception;

/**
 * Исключение при ошибках аутентификации
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String code, String message) {
        super(code, message);
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("AUTH_INVALID_TOKEN", "Неверный или просроченный токен");
    }

    public static AuthenticationException accessDenied() {
        return new AuthenticationException("AUTH_ACCESS_DENIED", "Недостаточно прав для выполнения операции");
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("AUTH_INVALID_CREDENTIALS", "Неверное имя пользователя или пароль");
    }

    public static AuthenticationException userDisabled() {
        return new AuthenticationException("AUTH_USER_DISABLED", "Учетная запись заблокирована");
    }
}
