package com.slavacom.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для создания профиля пользователя в организации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID organizationId;

    @NotBlank
    private String name;

    private String description;
}
