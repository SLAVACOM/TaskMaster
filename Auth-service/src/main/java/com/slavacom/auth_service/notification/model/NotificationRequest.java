package com.slavacom.auth_service.notification.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private UUID userId;

    @Email(message = "Email должен быть корректным")
    private String email;

    private Long telegramId;

    @NotBlank(message = "Заголовок не должен быть пустым")
    private String title;

    @NotBlank(message = "Сообщение не должно быть пустым")
    private String message;

    @NotEmpty(message = "Должен быть хотя бы один канал")
    private List<Channel> channels;
}
