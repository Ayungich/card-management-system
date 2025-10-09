package com.ayungi.cms.service;

import com.ayungi.cms.dto.mapper.TransactionMapper;
import com.ayungi.cms.dto.request.TransferRequest;
import com.ayungi.cms.dto.response.TransactionResponse;
import com.ayungi.cms.entity.Card;
import com.ayungi.cms.entity.Transaction;
import com.ayungi.cms.entity.User;
import com.ayungi.cms.entity.enums.AuditAction;
import com.ayungi.cms.entity.enums.TransactionStatus;
import com.ayungi.cms.repository.CardRepository;
import com.ayungi.cms.repository.TransactionRepository;
import com.ayungi.cms.util.CardMaskUtil;
import com.ayungi.cms.util.CardValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для переводов между картами
 */
@Service
@Slf4j
public class TransferService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CardValidator cardValidator;
    private final CardMaskUtil cardMaskUtil;
    private final AuditService auditService;

    public TransferService(
            CardRepository cardRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            CardValidator cardValidator,
            CardMaskUtil cardMaskUtil,
            AuditService auditService) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.cardValidator = cardValidator;
        this.cardMaskUtil = cardMaskUtil;
        this.auditService = auditService;
    }

    /**
     * Перевод средств между картами
     */
    @Transactional
    public TransactionResponse transfer(TransferRequest request, User currentUser) {
        log.info("Перевод {} с карты {} на карту {}", 
                request.getAmount(), request.getFromCardId(), request.getToCardId());

        // Загрузка карт
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("Карта-источник не найдена"));

        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new RuntimeException("Карта-получатель не найдена"));

        // Проверка, что обе карты принадлежат текущему пользователю
        if (!cardValidator.isSameOwner(fromCard, toCard)) {
            String error = "Переводы разрешены только между собственными картами";
            log.warn(error);
            return createFailedTransaction(fromCard, toCard, request.getAmount(), error);
        }

        if (!fromCard.getOwner().getId().equals(currentUser.getId())) {
            String error = "Карта-источник не принадлежит текущему пользователю";
            log.warn(error);
            return createFailedTransaction(fromCard, toCard, request.getAmount(), error);
        }

        // Валидация перевода
        String validationError = cardValidator.validateTransfer(fromCard, toCard, request.getAmount());
        if (validationError != null) {
            log.warn("Ошибка валидации перевода: {}", validationError);
            return createFailedTransaction(fromCard, toCard, request.getAmount(), validationError);
        }

        // Выполнение перевода
        try {
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            Transaction transaction = Transaction.builder()
                    .fromCard(fromCard)
                    .toCard(toCard)
                    .amount(request.getAmount())
                    .status(TransactionStatus.SUCCESS)
                    .build();

            transaction = transactionRepository.save(transaction);

            log.info("Перевод {} успешно выполнен", request.getAmount());

            auditService.logAction(currentUser, AuditAction.TRANSFER, "Transaction", 
                    transaction.getId().toString(),
                    String.format("Перевод %s с карты %s на карту %s", 
                            request.getAmount(),
                            cardMaskUtil.maskCardNumber(fromCard.getCardNumber()),
                            cardMaskUtil.maskCardNumber(toCard.getCardNumber())),
                    null);

            return transactionMapper.toResponse(transaction);

        } catch (Exception e) {
            log.error("Ошибка выполнения перевода", e);
            return createFailedTransaction(fromCard, toCard, request.getAmount(), 
                    "Внутренняя ошибка при выполнении перевода");
        }
    }

    /**
     * Создание записи о неудачной транзакции
     */
    private TransactionResponse createFailedTransaction(Card fromCard, Card toCard, 
                                                       BigDecimal amount, String reason) {
        Transaction transaction = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .status(TransactionStatus.FAILED)
                .failureReason(reason)
                .build();

        transaction = transactionRepository.save(transaction);
        
        return transactionMapper.toResponse(transaction);
    }

    /**
     * Получение истории транзакций пользователя
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.debug("Получение истории транзакций пользователя: {}", userId);

        return transactionRepository.findByOwnerId(userId, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Получение транзакций карты
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getCardTransactions(UUID cardId, User currentUser, Pageable pageable) {
        log.debug("Получение транзакций карты: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        // Проверка прав доступа
        if (!card.getOwner().getId().equals(currentUser.getId()) &&
            currentUser.getRoles().stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            throw new RuntimeException("Недостаточно прав для просмотра транзакций");
        }

        return transactionRepository.findByCardId(cardId, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Получение транзакции по ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, User currentUser) {
        log.debug("Получение транзакции по ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена"));

        // Проверка прав доступа
        boolean hasAccess = transaction.getFromCard().getOwner().getId().equals(currentUser.getId()) ||
                           transaction.getToCard().getOwner().getId().equals(currentUser.getId()) ||
                           currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));

        if (!hasAccess) {
            throw new RuntimeException("Недостаточно прав для просмотра транзакции");
        }

        return transactionMapper.toResponse(transaction);
    }

    /**
     * Получение транзакций по периоду
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByPeriod(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Получение транзакций за период: {} - {}", startDate, endDate);

        return transactionRepository.findByTimestampBetween(startDate, endDate, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Получение транзакций по статусу
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByStatus(
            TransactionStatus status, Pageable pageable) {
        log.debug("Получение транзакций по статусу: {}", status);

        return transactionRepository.findByStatus(status, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Получение всех транзакций с фильтрацией (для ADMIN)
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(
            TransactionStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Получение всех транзакций с фильтрами");

        String statusStr = status != null ? status.name() : null;
        return transactionRepository.findAllWithFilters(statusStr, startDate, endDate, pageable)
                .map(transactionMapper::toResponse);
    }

    /**
     * Получение статистики по транзакциям карты
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutgoingAmount(UUID cardId) {
        return transactionRepository.getTotalAmountByFromCardIdAndStatus(cardId, TransactionStatus.SUCCESS);
    }

    /**
     * Получение статистики по входящим транзакциям карты
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomingAmount(UUID cardId) {
        return transactionRepository.getTotalAmountByToCardIdAndStatus(cardId, TransactionStatus.SUCCESS);
    }
}
