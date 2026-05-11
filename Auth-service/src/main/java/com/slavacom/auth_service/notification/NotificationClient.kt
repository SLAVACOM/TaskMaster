package com.slavacom.auth_service.notification

import com.slavacom.notificationservice.model.Channel
import com.slavacom.notificationservice.model.NotificationRequest
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

@Component
class NotificationClient(
    @Qualifier("notificationServiceRestClient")
    private val restClient: RestClient
) {

    fun sendEmail(
        template: EmailTemplate,
        recipientEmail: String,
        subject: String,
        params: Map<String, String> = emptyMap()
    ) {
        try {
            val emailContent = EmailContent(template, recipientEmail, subject, params)

            val request = NotificationRequest(
                userId = null,
                email = recipientEmail,
                telegramId = null,
                title = subject,
                message = emailContent.getBody(),
                channels = listOf(Channel.EMAIL)
            )

            restClient.post()
                .uri("/notifications")
                .body(request)
                .retrieve()
                .toBodilessEntity()

            logger.info { "Email sent successfully to $recipientEmail (template: ${template.name})" }
        } catch (e: RestClientException) {
            logger.error(e) { "Failed to send email to $recipientEmail" }
            // Not throwing - email is not critical for registration
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error sending email to $recipientEmail" }
        }
    }

    fun sendWelcomeEmail(userName: String, email: String, appUrl: String) {
        sendEmail(
            template = EmailTemplate.WELCOME_REGISTRATION,
            recipientEmail = email,
            subject = "Добро пожаловать в TaskMaster!",
            params = mapOf(
                "userName" to userName,
                "appUrl" to appUrl
            )
        )
    }

    fun sendEmailVerification(userName: String, email: String, verificationUrl: String) {
        sendEmail(
            template = EmailTemplate.EMAIL_VERIFICATION,
            recipientEmail = email,
            subject = "Подтверждение электронной почты - TaskMaster",
            params = mapOf(
                "userName" to userName,
                "verificationUrl" to verificationUrl
            )
        )
    }

    fun sendInvitationEmail(
        invitedUserName: String,
        invitedEmail: String,
        invitedByName: String,
        organizationName: String,
        role: String,
        message: String?,
        expiresAt: String,
        acceptUrl: String,
        declineUrl: String
    ) {
        sendEmail(
            template = EmailTemplate.INVITATION_EMPLOYEE,
            recipientEmail = invitedEmail,
            subject = "Приглашение в $organizationName - TaskMaster",
            params = mapOf(
                "invitedUserName" to invitedUserName,
                "invitedByName" to invitedByName,
                "organizationName" to organizationName,
                "role" to role,
                "message" to (message ?: ""),
                "expiresAt" to expiresAt,
                "acceptUrl" to acceptUrl,
                "declineUrl" to declineUrl
            )
        )
    }
}
