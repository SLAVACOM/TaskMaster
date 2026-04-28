package com.slavacom.organizationservice.dto.old;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvitationRequest {

    @NotBlank(message = "Identifier (username or email) is required")
    private String identifier;

    private String role;

    private String permissions;

    private String message;
}

