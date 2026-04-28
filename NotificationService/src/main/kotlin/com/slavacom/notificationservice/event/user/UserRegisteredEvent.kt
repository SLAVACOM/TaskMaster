package com.slavacom.notificationservice.event.user

import com.slavacom.notificationservice.event.NotificationEvent
import java.util.UUID

data class UserRegisteredEvent (
    override val eventId: UUID,
    val userId: UUID,
    val name: String
) : NotificationEvent

