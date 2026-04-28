package com.slavacom.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для проверки возможности регистрации пользователя
 */
public record CanRegisterRequest(
        @NotNull @Email String email,
        @NotNull @NotBlank String username
) {
}
