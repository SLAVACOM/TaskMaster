package com.slavacom.organizationservice.dto.old;

import com.slavacom.organizationservice.entity.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {
    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private UUID invitedUserId;
    private UUID invitedByUserId;
    private String identifier;
    private String role;
    private String permissions;
    private String message;
    private InvitationStatus status;
    private Instant expiresAt;
    private Instant respondedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

