package com.slavacom.notificationservice.event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.slavacom.notificationservice.event.user.UserLoginEvent
import com.slavacom.notificationservice.event.user.UserRegisteredEvent
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
//    JsonSubTypes.Type(value = UserLoginEvent::class, name = "USER_LOGIN_EVENT"),
    JsonSubTypes.Type(value = UserRegisteredEvent::class, name = "UserRegisteredEvent")
)
interface  NotificationEvent{
    val eventId: UUID
}