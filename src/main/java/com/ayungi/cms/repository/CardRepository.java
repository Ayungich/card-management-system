package com.ayungi.cms.repository;

import com.ayungi.cms.entity.Card;
import com.ayungi.cms.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с банковскими картами
 */
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    /**
     * Поиск карты по номеру
     *
     * @param cardNumber номер карты (зашифрованный)
     * @return Optional с картой
     */
    Optional<Card> findByCardNumber(String cardNumber);

    /**
     * Поиск всех карт владельца
     *
     * @param ownerId ID владельца
     * @param pageable параметры пагинации
     * @return страница карт
     */
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId")
    Page<Card> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * Поиск карт владельца по статусу
     *
     * @param ownerId ID владельца
     * @param status статус карты
     * @param pageable параметры пагинации
     * @return страница карт
     */
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.status = :status")
    Page<Card> findByOwnerIdAndStatus(@Param("ownerId") UUID ownerId, 
                                       @Param("status") CardStatus status, 
                                       Pageable pageable);

    /**
     * Поиск конкретной карты владельца
     *
     * @param ownerId ID владельца
     * @param cardId ID карты
     * @return Optional с картой
     */
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.id = :cardId")
    Optional<Card> findByOwnerIdAndId(@Param("ownerId") UUID ownerId, @Param("cardId") UUID cardId);

    /**
     * Поиск всех карт по статусу
     *
     * @param status статус карты
     * @param pageable параметры пагинации
     * @return страница карт
     */
    Page<Card> findByStatus(CardStatus status, Pageable pageable);

    /**
     * Поиск карт с истекшим сроком действия
     *
     * @param currentDate текущая дата
     * @return список карт
     */
    @Query("SELECT c FROM Card c WHERE c.expirationDate < :currentDate AND c.status != 'EXPIRED'")
    List<Card> findExpiredCards(@Param("currentDate") LocalDate currentDate);

    /**
     * Поиск активных карт владельца
     *
     * @param ownerId ID владельца
     * @return список карт
     */
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Проверка существования карты по номеру
     *
     * @param cardNumber номер карты
     * @return true если карта существует
     */
    boolean existsByCardNumber(String cardNumber);

    /**
     * Подсчет количества карт владельца
     *
     * @param ownerId ID владельца
     * @return количество карт
     */
    @Query("SELECT COUNT(c) FROM Card c WHERE c.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Подсчет количества карт по статусу
     *
     * @param status статус карты
     * @return количество карт
     */
    long countByStatus(CardStatus status);

    /**
     * Поиск карт с балансом больше указанной суммы
     *
     * @param amount минимальная сумма
     * @param pageable параметры пагинации
     * @return страница карт
     */
    @Query("SELECT c FROM Card c WHERE c.balance >= :amount")
    Page<Card> findByBalanceGreaterThanEqual(@Param("amount") BigDecimal amount, Pageable pageable);

    /**
     * Получение общего баланса всех карт пользователя
     *
     * @param ownerId ID владельца
     * @return общий баланс
     */
    @Query("SELECT COALESCE(SUM(c.balance), 0) FROM Card c WHERE c.owner.id = :ownerId")
    BigDecimal getTotalBalanceByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Поиск всех карт с фильтрацией (для админа)
     *
     * @param status статус карты (опционально)
     * @param ownerId ID владельца (опционально)
     * @param pageable параметры пагинации
     * @return страница карт
     */
    @Query("SELECT c FROM Card c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:ownerId IS NULL OR c.owner.id = :ownerId)")
    Page<Card> findAllWithFilters(@Param("status") CardStatus status,
                                   @Param("ownerId") UUID ownerId,
                                   Pageable pageable);
}
