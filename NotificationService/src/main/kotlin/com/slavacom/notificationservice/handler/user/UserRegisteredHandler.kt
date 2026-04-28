package com.slavacom.notificationservice.handler.user

import com.slavacom.notificationservice.event.user.UserRegisteredEvent
import com.slavacom.notificationservice.handler.NotificationEventHandler
import com.slavacom.notificationservice.model.Channel
import com.slavacom.notificationservice.model.NotificationRequest
import com.slavacom.notificationservice.service.NotificationService
import com.slavacom.notificationservice.service.sender.EmailSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UserRegisteredHandler(
    private val notificationService: NotificationService,
    private val emailSender: EmailSender
) : NotificationEventHandler<UserRegisteredEvent> {

    override fun eventType() = UserRegisteredEvent::class.java

    override fun handle(event: UserRegisteredEvent) {
        val notification = NotificationRequest(

            userId = event.userId,
            title = "Welcome!",
            message = "Hello ${event.name}",
            channels = listOf(Channel.EMAIL, Channel.SMS),
            telegramId = 876395480,
            email = "slavacom121@gmail.com"
        )

        notificationService.send(notification)
    }


}