package com.ayungi.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO для ответа с информацией о пользователе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    
    private String username;
    
    private String email;
    
    private Set<String> roles;
    
    private Boolean enabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer cardsCount;
}
