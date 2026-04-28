package com.slavacom.organizationservice.dto.old;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID accountable;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // Дополнительная информация
    private long employeesCount;
    private long projectsCount;
    private List<ProjectTagsDto> availableTags;
}
