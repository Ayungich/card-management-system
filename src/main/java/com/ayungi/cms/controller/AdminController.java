package com.ayungi.cms.controller;

import com.ayungi.cms.dto.response.*;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.CardStatus;
import com.ayungi.cms.entity.enums.TransactionStatus;
import com.ayungi.cms.repository.CardRepository;
import com.ayungi.cms.repository.TransactionRepository;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.service.AuditService;
import com.ayungi.cms.service.CardService;
import com.ayungi.cms.service.TransferService;
import com.ayungi.cms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Контроллер для административных операций
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Административные операции")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    private final UserService userService;
    private final CardService cardService;
    private final TransferService transferService;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    public AdminController(
            UserService userService,
            CardService cardService,
            TransferService transferService,
            AuditService auditService,
            UserRepository userRepository,
            CardRepository cardRepository,
            TransactionRepository transactionRepository) {
        this.userService = userService;
        this.cardService = cardService;
        this.transferService = transferService;
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping("/users")
    @Operation(summary = "Список пользователей", description = "Получение списка всех пользователей")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос списка всех пользователей");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Получение пользователя по ID
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "Получение пользователя", description = "Получение информации о пользователе по ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("Запрос пользователя: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление пользователя
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удаление пользователя", description = "Удаление пользователя из системы")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на удаление пользователя: {}", id);
        User currentUser = getCurrentUser(authentication);
        userService.deleteUser(id, currentUser);
        return ResponseEntity.ok(MessageResponse.of("Пользователь успешно удален"));
    }

    /**
     * Блокировка/разблокировка пользователя
     */
    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Изменение статуса пользователя", description = "Блокировка или разблокировка пользователя")
    public ResponseEntity<UserResponse> toggleUserStatus(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на изменение статуса пользователя: {}", id);
        User currentUser = getCurrentUser(authentication);
        UserResponse response = userService.toggleUserStatus(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение всех карт с фильтрацией
     */
    @GetMapping("/cards")
    @Operation(summary = "Список всех карт", description = "Получение списка всех карт с фильтрацией")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос списка всех карт");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CardResponse> cards = cardService.getAllCards(status, ownerId, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Получение всех транзакций с фильтрацией
     */
    @GetMapping("/transactions")
    @Operation(summary = "Список всех транзакций", description = "Получение списка всех транзакций с фильтрацией")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос списка всех транзакций");
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<TransactionResponse> transactions = transferService.getAllTransactions(status, startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Получение логов аудита
     */
    @GetMapping("/audit-logs")
    @Operation(summary = "Логи аудита", description = "Получение системных логов аудита")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Запрос логов аудита");
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLogResponse> logs = auditService.getAllLogs(
                userId, 
                action != null ? com.ayungi.cms.entity.enums.AuditAction.valueOf(action) : null,
                entityType, 
                startDate, 
                endDate, 
                pageable
        );
        return ResponseEntity.ok(logs);
    }

    /**
     * Получение статистики системы
     */
    @GetMapping("/statistics")
    @Operation(summary = "Статистика системы", description = "Получение общей статистики по системе")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        log.info("Запрос статистики системы");
        
        long totalUsers = userRepository.count();
        long totalCards = cardRepository.count();
        long activeCards = cardRepository.countByStatus(CardStatus.ACTIVE);
        long blockedCards = cardRepository.countByStatus(CardStatus.BLOCKED);
        long expiredCards = cardRepository.countByStatus(CardStatus.EXPIRED);
        long totalTransactions = transactionRepository.count();
        long successfulTransactions = transactionRepository.countByStatus(TransactionStatus.SUCCESS);
        long failedTransactions = transactionRepository.countByStatus(TransactionStatus.FAILED);
        
        BigDecimal totalBalance = cardRepository.findAll().stream()
                .map(card -> card.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalTransactionAmount = transactionRepository.findByStatus(TransactionStatus.SUCCESS, Pageable.unpaged())
                .stream()
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        StatisticsResponse statistics = StatisticsResponse.builder()
                .totalUsers(totalUsers)
                .totalCards(totalCards)
                .activeCards(activeCards)
                .blockedCards(blockedCards)
                .expiredCards(expiredCards)
                .totalTransactions(totalTransactions)
                .successfulTransactions(successfulTransactions)
                .failedTransactions(failedTransactions)
                .totalBalance(totalBalance)
                .totalTransactionAmount(totalTransactionAmount)
                .build();
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * Поиск пользователей
     */
    @GetMapping("/users/search")
    @Operation(summary = "Поиск пользователей", description = "Поиск пользователей по имени или email")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Поиск пользователей: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Получение текущего пользователя
     */
    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
