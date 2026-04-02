package com.slavacom.authservice.exception;

import java.util.UUID;

public class UserAlreadyExistsException extends RuntimeException {
	public UserAlreadyExistsException(UUID userId) {
		super("User already exists with id: " + userId);
	}

	public UserAlreadyExistsException(String message) {
		super(message);
	}
}

