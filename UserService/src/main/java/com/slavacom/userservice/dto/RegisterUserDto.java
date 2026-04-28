package com.slavacom.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для регистрации пользователя без указания ID - ID генерируется автоматически
 */
public record RegisterUserDto(@NotNull @NotBlank String firstName,
                             @NotNull @NotBlank String lastName,
                             @NotNull @Email String email,
                             @NotBlank String username) {
}
