package com.slavacom.authservice.exception;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super("Invalid credentials provided");
	}

	public InvalidCredentialsException(String message) {
		super(message);
	}
}

