package com.slavacom.organizationservice.dto.old;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddEmployeeRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    private UUID profileId;

    @NotBlank(message = "role is required")
    private String role; // ADMIN | MANAGER | MEMBER

    private String permissions; // JSON, по умолчанию {}
}

