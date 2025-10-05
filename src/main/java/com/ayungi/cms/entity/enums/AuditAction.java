package com.ayungi.cms.entity.enums;

/**
 * Типы действий для системного аудита
 */
public enum AuditAction {
    /**
     * Создание сущности
     */
    CREATE,

    /**
     * Обновление сущности
     */
    UPDATE,

    /**
     * Удаление сущности
     */
    DELETE,

    /**
     * Блокировка карты
     */
    BLOCK,

    /**
     * Активация карты
     */
    ACTIVATE,

    /**
     * Перевод средств между картами
     */
    TRANSFER,

    /**
     * Вход в систему
     */
    LOGIN,

    /**
     * Выход из системы
     */
    LOGOUT
}
