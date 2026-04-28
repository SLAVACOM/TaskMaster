package com.slavacom.notificationservice.kafka.listener

import com.slavacom.notificationservice.event.NotificationEvent
import com.slavacom.notificationservice.event.user.UserLoginEvent

import com.slavacom.notificationservice.kafka.router.SafeEventRouter
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationKafkaListener(
    private val router: SafeEventRouter
) {

    @KafkaListener(topics = ["notifications.events"])
    fun listen(event: NotificationEvent) {
        router.route(event)
    }

    @KafkaListener(topics = ["user-login"])
    fun listenDlt(event: UserLoginEvent) {
        println(event)
    }
}