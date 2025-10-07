package com.ayungi.cms.repository;

import com.ayungi.cms.entity.Transaction;
import com.ayungi.cms.entity.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с транзакциями
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Поиск всех транзакций карты (исходящие)
     *
     * @param fromCardId ID карты-источника
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromCard.id = :fromCardId")
    Page<Transaction> findByFromCardId(@Param("fromCardId") UUID fromCardId, Pageable pageable);

    /**
     * Поиск всех транзакций карты (входящие)
     *
     * @param toCardId ID карты-получателя
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.toCard.id = :toCardId")
    Page<Transaction> findByToCardId(@Param("toCardId") UUID toCardId, Pageable pageable);

    /**
     * Поиск всех транзакций карты (исходящие и входящие)
     *
     * @param cardId ID карты
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId")
    Page<Transaction> findByCardId(@Param("cardId") UUID cardId, Pageable pageable);

    /**
     * Поиск транзакций владельца карт
     *
     * @param ownerId ID владельца
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromCard.owner.id = :ownerId OR t.toCard.owner.id = :ownerId")
    Page<Transaction> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * Поиск транзакций по периоду времени
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.timestamp BETWEEN :startDate AND :endDate")
    Page<Transaction> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    /**
     * Поиск транзакций по статусу
     *
     * @param status статус транзакции
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    /**
     * Поиск успешных транзакций карты
     *
     * @param cardId ID карты
     * @param status статус транзакции
     * @return список транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromCard.id = :cardId OR t.toCard.id = :cardId) AND t.status = :status")
    List<Transaction> findByCardIdAndStatus(@Param("cardId") UUID cardId, @Param("status") TransactionStatus status);

    /**
     * Поиск последних N транзакций карты
     *
     * @param cardId ID карты
     * @param pageable параметры пагинации (с ограничением)
     * @return список транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactionsByCardId(@Param("cardId") UUID cardId, Pageable pageable);

    /**
     * Подсчет количества транзакций по статусу
     *
     * @param status статус транзакции
     * @return количество транзакций
     */
    long countByStatus(TransactionStatus status);

    /**
     * Подсчет общей суммы успешных транзакций карты (исходящие)
     *
     * @param fromCardId ID карты-источника
     * @param status статус транзакции
     * @return общая сумма
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromCard.id = :fromCardId AND t.status = :status")
    BigDecimal getTotalAmountByFromCardIdAndStatus(@Param("fromCardId") UUID fromCardId, 
                                                    @Param("status") TransactionStatus status);

    /**
     * Подсчет общей суммы успешных транзакций карты (входящие)
     *
     * @param toCardId ID карты-получателя
     * @param status статус транзакции
     * @return общая сумма
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toCard.id = :toCardId AND t.status = :status")
    BigDecimal getTotalAmountByToCardIdAndStatus(@Param("toCardId") UUID toCardId, 
                                                  @Param("status") TransactionStatus status);

    /**
     * Поиск неудачных транзакций за период
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список транзакций
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.timestamp BETWEEN :startDate AND :endDate")
    List<Transaction> findFailedTransactionsBetween(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Поиск всех транзакций с фильтрацией (для админа)
     *
     * @param status статус (опционально)
     * @param startDate начальная дата (опционально)
     * @param endDate конечная дата (опционально)
     * @param pageable параметры пагинации
     * @return страница транзакций
     */
    @Query(value = "SELECT * FROM transactions t WHERE " +
                   "(:status IS NULL OR t.status = :status) AND " +
                   "(:startDate IS NULL OR t.timestamp >= :startDate) AND " +
                   "(:endDate IS NULL OR t.timestamp <= :endDate) " +
                   "ORDER BY t.timestamp DESC",
           countQuery = "SELECT COUNT(*) FROM transactions t WHERE " +
                       "(:status IS NULL OR t.status = :status) AND " +
                       "(:startDate IS NULL OR t.timestamp >= :startDate) AND " +
                       "(:endDate IS NULL OR t.timestamp <= :endDate)",
           nativeQuery = true)
    Page<Transaction> findAllWithFilters(@Param("status") TransactionStatus status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);
}
