package com.slavacom.notificationservice.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.util.UUID
import kotlin.uuid.Uuid

data class NotificationRequest(
    val userId: UUID?,

    @field:Email(message = "Email должен быть корректным")
    val email: String?,
    val telegramId: Long?,

    @field:NotBlank(message = "Заголовок не должен быть пустым")
    val title: String?,

    @field:NotBlank(message = "Сообщение не должно быть пустым")
    val message: String?,

    @field:NotEmpty(message = "Должен быть хотя бы один канал")
    val channels: List<Channel>?
)