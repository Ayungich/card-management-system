package com.ayungi.cms.dto.mapper;

import com.ayungi.cms.dto.response.TransactionResponse;
import com.ayungi.cms.entity.Transaction;
import com.ayungi.cms.util.CardMaskUtil;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования Transaction в DTO
 */
@Component
public class TransactionMapper {

    private final CardMaskUtil cardMaskUtil;

    public TransactionMapper(CardMaskUtil cardMaskUtil) {
        this.cardMaskUtil = cardMaskUtil;
    }

    /**
     * Преобразование Transaction в TransactionResponse
     */
    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .fromCardId(transaction.getFromCard().getId())
                .fromCardMaskedNumber(cardMaskUtil.maskCardNumber(transaction.getFromCard().getCardNumber()))
                .toCardId(transaction.getToCard().getId())
                .toCardMaskedNumber(cardMaskUtil.maskCardNumber(transaction.getToCard().getCardNumber()))
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
