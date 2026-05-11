package com.slavacom.userservice.exception;

public class OrganizationNotFoundException extends RuntimeException {
    private final String organizationId;

    public OrganizationNotFoundException(String organizationId) {
        super("Organization with ID " + organizationId + " not found");
        this.organizationId = organizationId;
    }

    public OrganizationNotFoundException(String organizationId, String message) {
        super(message);
        this.organizationId = organizationId;
    }

    public String getOrganizationId() {
        return organizationId;
    }
}
