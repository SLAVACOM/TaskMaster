package com.slavacom.userservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight DTO for profile details response - used for profile display
 */
public record ProfileDetailDto(
        UUID id,
        UUID userId,
        UUID organizationId,
        String name,
        String description,
        Boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
