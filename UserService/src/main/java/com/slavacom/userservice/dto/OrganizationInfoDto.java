package com.slavacom.userservice.dto;

/**
 * Информация об организации для ExtendedUserInfoDto
 */
public record OrganizationInfoDto(
        String id,
        String name,
        String description,
        String userId,
        String currentUserId,
        String role
) {
}
