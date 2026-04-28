package com.slavacom.organizationservice.dto.old;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    private String name;

    private String description;

    // accountable (владелец) берется из JWT токена
}
