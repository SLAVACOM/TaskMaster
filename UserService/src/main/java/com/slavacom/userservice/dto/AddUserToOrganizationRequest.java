package com.slavacom.user_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO для добавления пользователя в организацию
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddUserToOrganizationRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID organizationId;

    private String profileName;

    private String profileDescription;

    @Builder.Default
    private Boolean createDefaultProfile = true;
}
