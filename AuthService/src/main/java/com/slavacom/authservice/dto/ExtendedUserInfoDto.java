package com.slavacom.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Расширенная информация о пользователе для токена
 * Содержит userId, последний profileId и organizationId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedUserInfoDto {
    private UUID userId;
    private UUID profileId;
    private UUID organizationId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Boolean active;
    private Boolean isEmailVerified; // Добавляем информацию о подтверждении email

    // Данные об организации
    private OrganizationInfoDto organization;
}
