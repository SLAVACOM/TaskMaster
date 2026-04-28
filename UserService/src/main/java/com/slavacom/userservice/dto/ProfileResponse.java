package com.slavacom.user_service.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO для ответа с информацией о профиле пользователя в организации
 */
public record ProfileResponse(
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
