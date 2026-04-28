package com.slavacom.notificationservice.handler

import com.slavacom.notificationservice.event.NotificationEvent

interface NotificationEventHandler<T : NotificationEvent> {

    fun eventType(): Class<T>

    fun handle(event: T)
}