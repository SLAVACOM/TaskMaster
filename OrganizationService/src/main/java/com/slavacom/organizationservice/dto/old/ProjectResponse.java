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
public class ProjectResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private boolean isActive;
    private UUID responsible;
    private Instant createdAt;
    private Instant updatedAt;

    // Дополнительная информация
    private long employeesCount;
    private long goalsCount;
    private long sprintsCount;
    private long activeSprintsCount;
    private List<ProjectTagsDto> availableTags;
    private ProjectSprintInfo activeSprint;

    @Data
    @Builder
    public static class ProjectSprintInfo {
        private UUID id;
        private String name;
        private Instant startDate;
        private Instant endDate;
        private String status;
    }
}
