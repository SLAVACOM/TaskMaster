package com.slavacom.user_service.dto;

/**
 * DTO для ответа на проверку возможности регистрации пользователя
 */
public record CanRegisterResponse(
        boolean canRegister,
        String reason
) {
}
