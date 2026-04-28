package com.slavacom.organizationservice.exception;

import java.util.UUID;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(UUID invitationId) {
        super("Invitation not found: " + invitationId);
    }
}

