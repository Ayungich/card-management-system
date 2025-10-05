package com.ayungi.cms.controller;

import com.ayungi.cms.dto.request.CardCreateRequest;
import com.ayungi.cms.dto.response.BalanceResponse;
import com.ayungi.cms.dto.response.CardResponse;
import com.ayungi.cms.dto.response.MessageResponse;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.service.CardService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления банковскими картами
 */
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Управление банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class CardController {

    private final CardService cardService;
    private final UserRepository userRepository;

    public CardController(CardService cardService, UserRepository userRepository) {
        this.cardService = cardService;
        this.userRepository = userRepository;
    }

    /**
     * Создание новой карты (только для ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание карты", description = "Создание новой банковской карты (только для администратора)")
    public ResponseEntity<CardResponse> createCard(
            @Valid @RequestBody CardCreateRequest request,
            Authentication authentication) {
        log.info("Запрос на создание карты от пользователя: {}", authentication.getName());
        User currentUser = getCurrentUser(authentication);
        CardResponse response = cardService.createCard(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Получение карт текущего пользователя
     */
    @GetMapping
    @Operation(summary = "Получение своих карт", description = "Получение списка карт текущего пользователя")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Запрос на получение карт пользователя: {}", authentication.getName());
        User currentUser = getCurrentUser(authentication);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CardResponse> cards = cardService.getUserCards(currentUser.getId(), pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Получение карты по ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получение карты", description = "Получение информации о карте по ID")
    public ResponseEntity<CardResponse> getCardById(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на получение карты: {}", id);
        User currentUser = getCurrentUser(authentication);
        CardResponse response = cardService.getCardById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение баланса карты
     */
    @GetMapping("/{id}/balance")
    @Operation(summary = "Получение баланса", description = "Получение текущего баланса карты")
    public ResponseEntity<BalanceResponse> getCardBalance(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на получение баланса карты: {}", id);
        User currentUser = getCurrentUser(authentication);
        BalanceResponse response = cardService.getCardBalance(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Блокировка карты
     */
    @PatchMapping("/{id}/block")
    @Operation(summary = "Блокировка карты", description = "Блокировка карты (пользователь может блокировать свои карты)")
    public ResponseEntity<CardResponse> blockCard(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на блокировку карты: {}", id);
        User currentUser = getCurrentUser(authentication);
        CardResponse response = cardService.blockCard(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Активация карты (только для ADMIN)
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активация карты", description = "Активация заблокированной карты (только для администратора)")
    public ResponseEntity<CardResponse> activateCard(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на активацию карты: {}", id);
        User currentUser = getCurrentUser(authentication);
        CardResponse response = cardService.activateCard(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Удаление карты (только для ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удаление карты", description = "Удаление карты из системы (только для администратора)")
    public ResponseEntity<MessageResponse> deleteCard(
            @PathVariable UUID id,
            Authentication authentication) {
        log.info("Запрос на удаление карты: {}", id);
        User currentUser = getCurrentUser(authentication);
        cardService.deleteCard(id, currentUser);
        return ResponseEntity.ok(MessageResponse.of("Карта успешно удалена"));
    }

    /**
     * Получение текущего пользователя
     */
    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
}
