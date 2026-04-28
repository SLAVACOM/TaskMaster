package com.slavacom.notificationservice.controller

import com.slavacom.notificationservice.model.NotificationRequest
import com.slavacom.notificationservice.service.NotificationService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications")
@Validated
class NotificationController(
    private val service: NotificationService
) {

    @PostMapping
    fun send(@RequestBody @Valid request: NotificationRequest) {
        service.send(request)
    }
}