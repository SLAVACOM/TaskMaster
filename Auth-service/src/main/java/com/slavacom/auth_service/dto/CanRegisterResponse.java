package com.slavacom.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа на проверку возможности регистрации пользователя
 */
public record CanRegisterResponse(
		boolean canRegister,
		String reason) {
}
