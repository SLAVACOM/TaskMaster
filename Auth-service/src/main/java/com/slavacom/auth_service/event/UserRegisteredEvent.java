package com.slavacom.auth_service.event;

import java.io.Serializable;


public record UserRegisteredEvent(
        String userId,
        String email,
        String firstName,
        String lastName
) implements Serializable {}