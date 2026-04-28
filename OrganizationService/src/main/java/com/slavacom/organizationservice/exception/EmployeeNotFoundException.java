package com.slavacom.organizationservice.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(UUID organizationId, UUID userId) {
        super("Employee not found in organization " + organizationId + " for user " + userId);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}

