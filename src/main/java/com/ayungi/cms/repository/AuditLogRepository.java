package com.ayungi.cms.repository;

import com.ayungi.cms.entity.AuditLog;
import com.ayungi.cms.entity.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с логами аудита
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Поиск всех логов пользователя
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId")
    Page<AuditLog> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Поиск логов по типу действия
     *
     * @param action тип действия
     * @param pageable параметры пагинации
     * @return страница логов
     */
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    /**
     * Поиск логов по типу сущности и ID сущности
     *
     * @param entityType тип сущности
     * @param entityId ID сущности
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    Page<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
                                                @Param("entityId") String entityId,
                                                Pageable pageable);

    /**
     * Поиск логов по периоду времени
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    /**
     * Поиск логов пользователя по типу действия
     *
     * @param userId ID пользователя
     * @param action тип действия
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action")
    Page<AuditLog> findByUserIdAndAction(@Param("userId") UUID userId,
                                          @Param("action") AuditAction action,
                                          Pageable pageable);

    /**
     * Поиск последних N логов пользователя
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации (с ограничением)
     * @return список логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Поиск логов по IP-адресу
     *
     * @param ipAddress IP-адрес
     * @param pageable параметры пагинации
     * @return страница логов
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * Подсчет количества действий пользователя
     *
     * @param userId ID пользователя
     * @return количество действий
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Подсчет количества действий по типу
     *
     * @param action тип действия
     * @return количество действий
     */
    long countByAction(AuditAction action);

    /**
     * Поиск логов по типу сущности
     *
     * @param entityType тип сущности
     * @param pageable параметры пагинации
     * @return страница логов
     */
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    /**
     * Поиск всех логов с фильтрацией (для админа)
     *
     * @param userId ID пользователя (опционально)
     * @param action тип действия (опционально)
     * @param entityType тип сущности (опционально)
     * @param startDate начальная дата (опционально)
     * @param endDate конечная дата (опционально)
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> findAllWithFilters(@Param("userId") UUID userId,
                                       @Param("action") AuditAction action,
                                       @Param("entityType") String entityType,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * Удаление старых логов (для очистки)
     *
     * @param beforeDate дата, до которой удалять логи
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :beforeDate")
    void deleteOldLogs(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Поиск логов входа пользователя
     *
     * @param userId ID пользователя
     * @param pageable параметры пагинации
     * @return страница логов
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'LOGIN' ORDER BY a.timestamp DESC")
    Page<AuditLog> findLoginHistoryByUserId(@Param("userId") UUID userId, Pageable pageable);
}
