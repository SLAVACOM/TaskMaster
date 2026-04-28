package com.slavacom.auth_service.exception;

import java.util.UUID;

public class UserAlreadyExistsException extends RuntimeException {
	public UserAlreadyExistsException(UUID userId) {
		super("User already exists with id: " + userId);
	}

	public UserAlreadyExistsException(String message) {
		super(message);
	}
}

