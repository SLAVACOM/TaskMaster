package com.slavacom.organizationservice.dto.old;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

//    @NotNull(message = "Responsible user ID is required")
    private UUID responsible;
}
