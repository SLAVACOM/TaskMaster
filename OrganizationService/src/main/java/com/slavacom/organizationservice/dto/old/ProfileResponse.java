package com.slavacom.organizationservice.dto.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private UUID userId;
    private UUID organizationId;
    private String name;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
