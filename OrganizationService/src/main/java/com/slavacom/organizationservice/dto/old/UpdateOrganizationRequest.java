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
public class UpdateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String description;
}

