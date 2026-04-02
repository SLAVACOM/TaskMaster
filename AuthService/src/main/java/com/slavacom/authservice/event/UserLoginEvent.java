package com.slavacom.authservice.event;

import java.util.UUID;

public record UserLoginEvent(UUID eventId, UUID userId, String email, String type) {
	public UserLoginEvent(UUID eventId, UUID userId, String email) {
		this(eventId, userId, email, "USER_LOGIN_EVENT");
	}
}