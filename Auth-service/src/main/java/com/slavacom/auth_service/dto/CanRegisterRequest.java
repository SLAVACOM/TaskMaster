package com.slavacom.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для проверки возможности регистрации пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanRegisterRequest {
    @NotNull
    @Email
    private String email;

    @NotNull
    @NotBlank
    private String username;
}
