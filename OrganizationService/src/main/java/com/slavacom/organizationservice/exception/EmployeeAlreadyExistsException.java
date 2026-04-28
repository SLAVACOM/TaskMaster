package com.slavacom.organizationservice.exception;

import java.util.UUID;

public class EmployeeAlreadyExistsException extends RuntimeException {
    public EmployeeAlreadyExistsException(UUID organizationId, UUID userId) {
        super("Employee already exists in organization " + organizationId + " for user " + userId);
    }
}

