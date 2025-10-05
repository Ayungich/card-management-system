package com.ayungi.cms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO для ответа при аутентификации
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    
    private String refreshToken;
    
    private String username;
    
    private String email;
    
    private Set<String> roles;
    
    @Builder.Default
    private String tokenType = "Bearer";
}
