package com.slavacom.organizationservice.notification.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class NotificationRequest(
    val userId: UUID? = null,

    @field:Email(message = "Email должен быть корректным")
    val email: String? = null,

    val telegramId: Long? = null,

    @field:NotBlank(message = "Заголовок не должен быть пустым")
    val title: String? = null,

    @field:NotBlank(message = "Сообщение не должно быть пустым")
    val message: String? = null,

    @field:NotEmpty(message = "Должен быть хотя бы один канал")
    val channels: List<Channel>? = null
)
