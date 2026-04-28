package com.slavacom.user_service.exception;

public class UserAlreadyExistBy extends RuntimeException {
	public UserAlreadyExistBy(String by, String value) {
		super(String.format("User with %s %s already exists. Cannot create user.", by, value));
	}
}
