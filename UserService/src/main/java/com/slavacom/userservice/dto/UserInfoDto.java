package com.slavacom.userservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для получения информации о пользователе (совместимо с Auth-service)
 */
public record UserInfoDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean active,
        UUID organizationId,
        UUID profileId, // lastProfileId из User entity
        Instant createdAt,
        Instant updatedAt
) {
}
