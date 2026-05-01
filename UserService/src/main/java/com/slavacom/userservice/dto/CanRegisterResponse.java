package com.slavacom.userservice.dto;

/**
 * DTO для ответа на проверку возможности регистрации пользователя
 */
public record CanRegisterResponse(
        boolean canRegister,
        String reason
) {
}
