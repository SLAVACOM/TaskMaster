package com.slavacom.notificationservice.service.sender

import com.slavacom.notificationservice.model.Channel
import com.slavacom.notificationservice.model.NotificationRequest

interface NotificationSender {

    fun support(channel: Channel): Boolean

    fun send(request: NotificationRequest)

}