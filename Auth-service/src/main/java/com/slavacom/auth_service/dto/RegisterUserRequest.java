package com.slavacom.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для регистрации пользователя в User Service
 */
@Data
@Builder
public class RegisterUserRequest {
    @NotNull
    @NotBlank
    private String firstName;

    @NotNull
    @NotBlank
    private String lastName;

    @NotNull
    @Email
    private String email;

    @NotBlank
    private String username;
}
