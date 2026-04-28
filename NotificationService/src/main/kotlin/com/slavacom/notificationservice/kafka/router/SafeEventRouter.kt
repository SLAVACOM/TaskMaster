package com.slavacom.notificationservice.kafka.router

import com.slavacom.notificationservice.event.NotificationEvent
import com.slavacom.notificationservice.idempotency.IdempotencyService
import org.springframework.stereotype.Component

@Component
class SafeEventRouter(
    private val router: EventRouter,
    private val idempotencyService: IdempotencyService
) {

    fun route(event: NotificationEvent) {

        idempotencyService.executeOnce(event.eventId.toString()) {
            router.route(event)
        }
    }
}