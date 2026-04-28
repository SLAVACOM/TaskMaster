package com.slavacom.notificationservice.event.user;

import java.util.*

@JvmRecord
data class UserLoginEvent(val eventId: UUID?, val userId: UUID?, val email: String?, val type: String?) {
    constructor(eventId: UUID?, userId: UUID?, email: String?) : this(eventId, userId, email, "USER_LOGIN_EVENT")
}