package com.ayungi.cms.controller;

import com.ayungi.cms.dto.request.TransferRequest;
import com.ayungi.cms.dto.response.TransactionResponse;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для переводов между картами
 */
@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Переводы между картами")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    public TransferController(TransferService transferService, UserRepository userRepository) {
        this.transferService = transferService;
        this.userRepository = userRepository;
    }

    /**
     * Перевод средств между своими картами
     */
    @PostMapping
    @Operation(summary = "Перевод средств", description = "Перевод средств между собственными картами")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        log.info("Запрос на перевод {} с карты {} на карту {}", 
                request.getAmount(), request.getFromCardId(), request.getToCardId());
        User currentUser = getCurrentUser(authentication);
        TransactionResponse response = transferService.transfer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * История транзакций текущего пользователя
     */
    @GetMapping("/history")
    @Operation(summary = "История переводов", description = "Получение истории всех переводов пользователя")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactionHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос истории транзакций пользователя: {}", authentication.getName());
        User currentUser = getCurrentUser(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<TransactionResponse> transactions = transferService.getUserTransactions(currentUser.getId(), pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * История транзакций конкретной карты
     */
    @GetMapping("/card/{cardId}")
    @Operation(summary = "История переводов карты", description = "Получение истории переводов конкретной карты")
    public ResponseEntity<Page<TransactionResponse>> getCardTransactionHistory(
            @PathVariable UUID cardId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос истории транзакций карты: {}", cardId);
        User currentUser = getCurrentUser(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<TransactionResponse> transactions = transferService.getCardTransactions(cardId, currentUser, pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Получение транзакции по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получение транзакции", description = "Получение информации о транзакции по ID")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос транзакции: {}", id);
        User currentUser = getCurrentUser(authentication);
        TransactionResponse response = transferService.getTransactionById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение текущего пользователя
     */
    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
