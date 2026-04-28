package com.slavacom.organizationservice.dto.old;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class ProjectHistoryResponse {
    UUID id;
    UUID projectId;
    UUID changedBy;
    Instant changedAt;
    String action;
    String changes;
}

