package com.slavacom.notificationservice.exception

import com.slavacom.notificationservice.event.NotificationEvent

class EventHandlerNotFound(event: NotificationEvent) : RuntimeException ("No handler found for event type:")
