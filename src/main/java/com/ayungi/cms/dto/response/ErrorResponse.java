package com.ayungi.cms.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с ошибкой
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Integer status;
    
    private String error;
    
    private String code;
    
    private String message;
    
    private String path;
    
    /**
     * Создание ответа об ошибке
     */
    public static ErrorResponse of(Integer status, String error, String code, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .code(code)
                .message(message)
                .path(path)
                .build();
    }
}
