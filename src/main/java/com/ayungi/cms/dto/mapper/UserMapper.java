package com.ayungi.cms.dto.mapper;

import com.ayungi.cms.dto.response.UserResponse;
import com.ayungi.cms.entity.Role;
import com.ayungi.cms.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Маппер для преобразования User в DTO
 */
@Component
public class UserMapper {

    /**
     * Преобразование User в UserResponse
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .cardsCount(user.getCards() != null ? user.getCards().size() : 0)
                .build();
    }
}
