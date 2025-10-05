package com.ayungi.cms.entity.enums;

/**
 * Статусы транзакции (перевода)
 */
public enum TransactionStatus {
    /**
     * Транзакция успешно выполнена
     */
    SUCCESS,

    /**
     * Транзакция не выполнена (недостаточно средств, карта заблокирована и т.д.)
     */
    FAILED
}
