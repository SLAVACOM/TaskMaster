package com.slavacom.authservice.dto;

/**
 * DTO для ответа на проверку возможности регистрации пользователя
 */
public record CanRegisterResponse(
		boolean canRegister,
		String reason) {
}
