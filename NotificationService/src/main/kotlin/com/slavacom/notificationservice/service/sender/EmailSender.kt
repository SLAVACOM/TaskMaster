package com.slavacom.notificationservice.service.sender

import com.slavacom.notificationservice.model.Channel
import com.slavacom.notificationservice.model.NotificationRequest
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailSender(
    private val mailSender: JavaMailSender
) : NotificationSender {

    override fun support(channel: Channel): Boolean {
        return channel == Channel.EMAIL
    }

    override fun send(request: NotificationRequest) {
        if (request.email.isNullOrBlank()) {
            throw IllegalArgumentException("Email is required for email notifications")
        }

        val message = SimpleMailMessage()

        message.setTo(request.email)
        message.subject = request.title
        message.text = request.message

        mailSender.send(message)

        println("Email sent to ${request.email} with title '${request.title}' and message '${request.message}'")
    }
}