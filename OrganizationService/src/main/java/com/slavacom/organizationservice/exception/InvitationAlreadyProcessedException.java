package com.slavacom.organizationservice.exception;

public class InvitationAlreadyProcessedException extends RuntimeException {
    public InvitationAlreadyProcessedException(String message) {
        super(message);
    }
}

