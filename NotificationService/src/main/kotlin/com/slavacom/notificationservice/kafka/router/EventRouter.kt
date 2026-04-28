package com.slavacom.notificationservice.kafka.router

import com.slavacom.notificationservice.event.NotificationEvent
import com.slavacom.notificationservice.handler.NotificationEventHandler
import com.slavacom.notificationservice.exception.EventHandlerNotFound
import org.springframework.stereotype.Component

@Component
class EventRouter(
    handlers: List<NotificationEventHandler<*>>
) {

    private val handlerMap =
        handlers.associateBy { it.eventType() }

    @Suppress("UNCHECKED_CAST")
    fun route(event: NotificationEvent) {

        val handler = handlerMap[event::class.java]
            ?: throw EventHandlerNotFound(event)

        (handler as NotificationEventHandler<NotificationEvent>)
            .handle(event)
    }
}