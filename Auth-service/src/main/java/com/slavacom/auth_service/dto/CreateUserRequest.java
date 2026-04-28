package com.slavacom.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO для создания пользователя в User Service через REST API с заданным ID
 */
@Data
@Builder
public class CreateUserRequest {
    @NotNull
    private UUID id;

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
