package com.ayungi.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для простого текстового ответа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private String message;
    
    public static MessageResponse of(String message) {
        return MessageResponse.builder()
                .message(message)
                .build();
    }
}
