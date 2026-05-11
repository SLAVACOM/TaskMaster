package com.slavacom.organizationservice.notification

import com.slavacom.organizationservice.notification.model.Channel
import com.slavacom.organizationservice.notification.model.NotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class NotificationClient(
    @Qualifier("notificationServiceRestClient")
    private val restClient: RestClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

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
        try {
            val request = NotificationRequest(
                userId = null,
                email = invitedEmail,
                telegramId = null,
                title = "Приглашение в $organizationName - TaskMaster",
                message = buildInvitationEmailBody(
                    invitedUserName,
                    invitedByName,
                    organizationName,
                    role,
                    message,
                    expiresAt,
                    acceptUrl,
                    declineUrl
                ),
                channels = listOf(Channel.EMAIL)
            )

            restClient.post()
                .uri("/notifications")
                .body(request)
                .retrieve()
                .toBodilessEntity()

            logger.info("Invitation email sent successfully to $invitedEmail for organization $organizationName")
        } catch (e: RestClientException) {
            logger.error("Failed to send invitation email to $invitedEmail", e)
        } catch (e: Exception) {
            logger.error("Unexpected error sending invitation email to $invitedEmail", e)
        }
    }

    private fun buildInvitationEmailBody(
        invitedUserName: String,
        invitedByName: String,
        organizationName: String,
        role: String,
        message: String?,
        expiresAt: String,
        acceptUrl: String,
        declineUrl: String
    ): String = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h1>Приглашение в организацию 🎉</h1>
            <p>Привет, $invitedUserName!</p>
            <p><strong>$invitedByName</strong> приглашает вас присоединиться к организации <strong>$organizationName</strong></p>

            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                <p><strong>Роль:</strong> $role</p>
                <p><strong>Сообщение:</strong> ${message ?: ""}</p>
                <p><strong>Приглашение действительно до:</strong> $expiresAt</p>
            </div>

            <p>
                <a href="$acceptUrl" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-right: 10px;">
                    ✅ Принять приглашение
                </a>
                <a href="$declineUrl" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                    ❌ Отклонить
                </a>
            </p>

            <p style="color: #666; font-size: 12px; margin-top: 30px;">
                С уважением,<br/>
                Команда TaskMaster
            </p>
        </body>
        </html>
    """.trimIndent()
}
