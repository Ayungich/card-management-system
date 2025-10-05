package com.ayungi.cms.dto.request;

import com.ayungi.cms.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для фильтрации карт
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardFilterRequest {

    private CardStatus status;
    
    private UUID ownerId;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
