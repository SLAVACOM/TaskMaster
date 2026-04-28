package com.slavacom.notificationservice.service.sender

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class NotificationBot(
    @Value($$"${telegram.bot.token}")
    private val token: String
) : TelegramLongPollingBot(token) {

    override fun getBotUsername() = "SlavacomNotificationBot"

    override fun onUpdateReceived(p0: Update?) {
        TODO("Not yet implemented")
    }
}