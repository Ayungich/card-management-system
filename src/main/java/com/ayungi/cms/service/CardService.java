package com.ayungi.cms.service;

import com.ayungi.cms.dto.mapper.CardMapper;
import com.ayungi.cms.dto.request.CardCreateRequest;
import com.ayungi.cms.dto.response.BalanceResponse;
import com.ayungi.cms.dto.response.CardResponse;
import com.ayungi.cms.entity.Card;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.AuditAction;
import com.ayungi.cms.entity.enums.CardStatus;
import com.ayungi.cms.repository.CardRepository;
import com.ayungi.cms.repository.UserRepository;
import com.ayungi.cms.util.CardMaskUtil;
import com.ayungi.cms.util.CardNumberGenerator;
import com.ayungi.cms.util.CardValidator;
import com.ayungi.cms.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления банковскими картами
 */
@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CardNumberGenerator cardNumberGenerator;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskUtil cardMaskUtil;
    private final CardValidator cardValidator;
    private final AuditService auditService;

    public CardService(
            CardRepository cardRepository,
            UserRepository userRepository,
            CardMapper cardMapper,
            CardNumberGenerator cardNumberGenerator,
            EncryptionUtil encryptionUtil,
            CardMaskUtil cardMaskUtil,
            CardValidator cardValidator,
            AuditService auditService) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.cardNumberGenerator = cardNumberGenerator;
        this.encryptionUtil = encryptionUtil;
        this.cardMaskUtil = cardMaskUtil;
        this.cardValidator = cardValidator;
        this.auditService = auditService;
    }

    /**
     * Создание новой карты (только для ADMIN)
     */
    @Transactional
    public CardResponse createCard(CardCreateRequest request, User currentUser) {
        log.info("Создание карты для пользователя: {}", request.getOwnerId());

        // Проверка существования владельца
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Владелец карты не найден"));

        // Генерация уникального номера карты
        String cardNumber;
        String encryptedCardNumber;
        do {
            cardNumber = cardNumberGenerator.generateCardNumber();
            encryptedCardNumber = encryptionUtil.encrypt(cardNumber);
        } while (cardRepository.existsByCardNumber(encryptedCardNumber));

        // Создание карты
        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .owner(owner)
                .expirationDate(request.getExpirationDate())
                .status(CardStatus.ACTIVE)
                .balance(request.getInitialBalance())
                .build();

        card = cardRepository.save(card);
        log.info("Карта {} успешно создана для пользователя {}", 
                cardMaskUtil.maskCardNumber(encryptedCardNumber), owner.getUsername());

        auditService.logAction(currentUser, AuditAction.CREATE, "Card", card.getId().toString(),
                "Создание карты для пользователя: " + owner.getUsername(), null);

        return cardMapper.toResponse(card);
    }

    /**
     * Получение карт пользователя
     */
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(UUID userId, Pageable pageable) {
        log.debug("Получение карт пользователя: {}", userId);

        return cardRepository.findByOwnerId(userId, pageable)
                .map(cardMapper::toResponse);
    }

    /**
     * Получение карты по ID
     */
    @Transactional(readOnly = true)
    public CardResponse getCardById(UUID cardId, User currentUser) {
        log.debug("Получение карты по ID: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверка прав доступа
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            throw new RuntimeException("Недостаточно прав для просмотра карты");
        }

        return cardMapper.toResponse(card);
    }

    /**
     * Получение баланса карты
     */
    @Transactional(readOnly = true)
    public BalanceResponse getCardBalance(UUID cardId, User currentUser) {
        log.debug("Получение баланса карты: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверка прав доступа
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            throw new RuntimeException("Недостаточно прав для просмотра баланса");
        }

        return BalanceResponse.builder()
                .cardId(card.getId())
                .maskedCardNumber(cardMaskUtil.maskCardNumber(card.getCardNumber()))
                .balance(card.getBalance())
                .build();
    }

    /**
     * Блокировка карты
     */
    @Transactional
    public CardResponse blockCard(UUID cardId, User currentUser) {
        log.info("Блокировка карты: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Пользователь может блокировать только свои карты, админ - любые
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            throw new RuntimeException("Недостаточно прав для блокировки карты");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new RuntimeException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        
        log.info("Карта {} успешно заблокирована", cardMaskUtil.maskCardNumber(card.getCardNumber()));

        auditService.logAction(currentUser, AuditAction.BLOCK, "Card", card.getId().toString(),
                "Блокировка карты", null);

        return cardMapper.toResponse(card);
    }

    /**
     * Активация карты (только для ADMIN)
     */
    @Transactional
    public CardResponse activateCard(UUID cardId, User currentUser) {
        log.info("Активация карты: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new RuntimeException("Карта уже активна");
        }

        if (cardValidator.isExpired(card)) {
            throw new RuntimeException("Невозможно активировать карту с истекшим сроком действия");
        }

        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        
        log.info("Карта {} успешно активирована", cardMaskUtil.maskCardNumber(card.getCardNumber()));

        auditService.logAction(currentUser, AuditAction.ACTIVATE, "Card", card.getId().toString(),
                "Активация карты", null);

        return cardMapper.toResponse(card);
    }

    /**
     * Удаление карты (только для ADMIN)
     */
    @Transactional
    public void deleteCard(UUID cardId, User currentUser) {
        log.info("Удаление карты: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        String maskedNumber = cardMaskUtil.maskCardNumber(card.getCardNumber());
        cardRepository.delete(card);
        
        log.info("Карта {} успешно удалена", maskedNumber);

        auditService.logAction(currentUser, AuditAction.DELETE, "Card", cardId.toString(),
                "Удаление карты: " + maskedNumber, null);
    }

    /**
     * Получение всех карт с фильтрацией (для ADMIN)
     */
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(CardStatus status, UUID ownerId, Pageable pageable) {
        log.debug("Получение всех карт с фильтрами");

        return cardRepository.findAllWithFilters(status, ownerId, pageable)
                .map(cardMapper::toResponse);
    }

    /**
     * Обновление статусов истекших карт
     */
    @Transactional
    public void updateExpiredCards() {
        log.info("Обновление статусов истекших карт");

        List<Card> expiredCards = cardRepository.findExpiredCards(LocalDate.now());
        
        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            log.debug("Карта {} помечена как истекшая", card.getId());
        }

        log.info("Обновлено {} истекших карт", expiredCards.size());
    }

    /**
     * Получение карт по статусу
     */
    @Transactional(readOnly = true)
    public Page<CardResponse> getCardsByStatus(CardStatus status, Pageable pageable) {
        log.debug("Получение карт по статусу: {}", status);

        return cardRepository.findByStatus(status, pageable)
                .map(cardMapper::toResponse);
    }
}
