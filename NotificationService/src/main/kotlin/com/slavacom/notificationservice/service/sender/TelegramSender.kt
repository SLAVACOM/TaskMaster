package com.slavacom.notificationservice.service.sender

import com.slavacom.notificationservice.model.Channel
import com.slavacom.notificationservice.model.NotificationRequest
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class TelegramSender(
    private val bot: NotificationBot
) : NotificationSender {

    override fun support(channel: Channel) =
        channel == Channel.TELEGRAM


    override fun send(request: NotificationRequest) {

        // TODO: Добавить поиск Telegram ID по userId, если он не указан в запросе
        val chatId = request.telegramId.toString()

        val message = SendMessage.builder()
            .chatId(chatId)
            .text(request.message ?: "")
            .build()

        bot.execute(message)
        println("Telegram message sent to ${request.telegramId} with title '${request.title}' and message '${request.message}'")
    }

}