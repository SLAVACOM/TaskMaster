package com.slavacom.organizationservice.dto.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация об организации для интеграции с User Service
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
