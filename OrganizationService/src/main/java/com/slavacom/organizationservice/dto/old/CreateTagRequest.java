package com.slavacom.organizationservice.dto.old;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTagRequest {

	private UUID projectId;
    private UUID organizationId;

    @NotBlank
    private String name;

    private String color;

    private String description;
}

