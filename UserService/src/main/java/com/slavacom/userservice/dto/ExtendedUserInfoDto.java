package com.slavacom.user_service.dto;

import java.util.UUID;

/**
 * Расширенная информация о пользователе для токена
 * Содержит userId, последний profileId и organizationId
 */
public record ExtendedUserInfoDto(
        UUID userId,
        UUID profileId,
        UUID organizationId,
        String firstName,
        String lastName,
        String email,
        String username,
        Boolean active,
        Boolean isEmailVerified,
        OrganizationInfoDto organization
) {
}
