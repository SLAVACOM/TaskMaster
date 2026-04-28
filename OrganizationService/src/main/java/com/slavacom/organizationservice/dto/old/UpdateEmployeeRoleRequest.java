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
public class UpdateEmployeeRoleRequest {

    @NotBlank(message = "Role is required")
    private String role;

    private String permissions;
}

