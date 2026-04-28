package com.slavacom.auth_service.event;

import java.util.UUID;

public record UserCreatedEvent(
		UUID userId,
		String email,
		String username,
		String firstName,
		String lastName) {
}

