package com.ayungi.cms.exception;

/**
 * Исключение при нарушении бизнес-правил
 */
public class BusinessException extends BaseException {

    public BusinessException(String code, String message) {
        super(code, message);
    }

    // Исключения для карт
    public static BusinessException cardBlocked() {
        return new BusinessException("CARD_BLOCKED", "Операция невозможна: карта заблокирована");
    }

    public static BusinessException cardExpired() {
        return new BusinessException("CARD_EXPIRED", "Операция невозможна: срок действия карты истек");
    }

    public static BusinessException cardNotActive() {
        return new BusinessException("CARD_NOT_ACTIVE", "Операция невозможна: карта не активна");
    }

    // Исключения для переводов
    public static BusinessException insufficientBalance() {
        return new BusinessException("INSUFFICIENT_BALANCE", "Недостаточно средств на карте");
    }

    public static BusinessException transferSelfOnly() {
        return new BusinessException("TRANSFER_SELF_ONLY", "Переводы разрешены только между собственными картами");
    }

    public static BusinessException transferSameCard() {
        return new BusinessException("TRANSFER_SAME_CARD", "Нельзя переводить средства на ту же карту");
    }

    public static BusinessException invalidAmount() {
        return new BusinessException("INVALID_AMOUNT", "Некорректная сумма перевода");
    }

    // Исключения для пользователей
    public static BusinessException usernameAlreadyExists() {
        return new BusinessException("USERNAME_EXISTS", "Пользователь с таким именем уже существует");
    }

    public static BusinessException emailAlreadyExists() {
        return new BusinessException("EMAIL_EXISTS", "Пользователь с таким email уже существует");
    }

    public static BusinessException cannotDeleteSelf() {
        return new BusinessException("CANNOT_DELETE_SELF", "Нельзя удалить собственный аккаунт");
    }

    public static BusinessException cannotModifySelf() {
        return new BusinessException("CANNOT_MODIFY_SELF", "Нельзя изменить статус собственного аккаунта");
    }
}
