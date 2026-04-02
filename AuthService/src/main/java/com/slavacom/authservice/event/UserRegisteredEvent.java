package com.slavacom.authservice.event;

import java.io.Serializable;


public record UserRegisteredEvent(
        String userId,
        String email,
        String firstName,
        String lastName
) implements Serializable {}