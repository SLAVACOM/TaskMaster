package com.slavacom.organizationservice.dto.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    private String name;
    private String description;
}
