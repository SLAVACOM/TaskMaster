package com.slavacom.organizationservice.dto.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private UUID id;
    private UUID userId;
    private UUID profileId;
    private UUID organizationId;
    private String role;
    private String permissions;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

