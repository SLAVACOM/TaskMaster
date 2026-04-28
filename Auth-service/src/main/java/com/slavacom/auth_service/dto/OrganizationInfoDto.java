package com.slavacom.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация об организации для AuthResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationInfoDto {
    private String id;
    private String name;
    private String description;
    private String userId;
    private String currentUserId;
    private String role;
}
