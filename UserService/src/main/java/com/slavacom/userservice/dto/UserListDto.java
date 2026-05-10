package com.slavacom.userservice.dto;

import java.util.UUID;

/**
 * Lightweight DTO for user list responses - used for dropdowns, search, and user cards
 */
public record UserListDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        UUID lastProfileId,
        UUID lastOrganizationId
) {
}
