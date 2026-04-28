package com.slavacom.organizationservice.dto.old;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.slavacom.organizationservice.entity.ProjectTags;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link ProjectTags}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTagsDto {
	private UUID id;
	private UUID projectId;
	@NotNull
	@NotEmpty
	@NotBlank
	private String name;
	private String color;
	private String description;
	private Instant createdAt;
	private Instant updatedAt;
}