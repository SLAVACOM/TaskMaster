package com.slavacom.notificationservice.service

import com.slavacom.notificationservice.model.NotificationRequest
import com.slavacom.notificationservice.service.sender.NotificationSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val senders: List<NotificationSender>
) {

    @Async
    fun send(notification: NotificationRequest) {

        notification.channels?.forEach { channel ->
            senders
                .first { it.support(channel) }
                .send(notification)
        }
    }
}