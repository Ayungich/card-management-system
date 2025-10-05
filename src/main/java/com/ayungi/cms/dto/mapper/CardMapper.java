package com.ayungi.cms.dto.mapper;

import com.ayungi.cms.dto.response.CardResponse;
import com.ayungi.cms.entity.Card;
import com.ayungi.cms.util.CardMaskUtil;
import org.springframework.stereotype.Component;

/**
 * Маппер для преобразования Card в DTO
 */
@Component
public class CardMapper {

    private final CardMaskUtil cardMaskUtil;

    public CardMapper(CardMaskUtil cardMaskUtil) {
        this.cardMaskUtil = cardMaskUtil;
    }

    /**
     * Преобразование Card в CardResponse
     */
    public CardResponse toResponse(Card card) {
        if (card == null) {
            return null;
        }

        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(cardMaskUtil.maskCardNumber(card.getCardNumber()))
                .ownerId(card.getOwner().getId())
                .ownerUsername(card.getOwner().getUsername())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .isExpired(card.isExpired())
                .isActive(card.isActive())
                .build();
    }

    /**
     * Преобразование Card в CardResponse (без информации о владельце)
     */
    public CardResponse toResponseWithoutOwner(Card card) {
        if (card == null) {
            return null;
        }

        return CardResponse.builder()
                .id(card.getId())
                .maskedCardNumber(cardMaskUtil.maskCardNumber(card.getCardNumber()))
                .ownerId(card.getOwner().getId())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .isExpired(card.isExpired())
                .isActive(card.isActive())
                .build();
    }
}
