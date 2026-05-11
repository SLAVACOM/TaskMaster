package com.slavacom.auth_service.notification;

import com.slavacom.auth_service.notification.model.Channel;
import com.slavacom.auth_service.notification.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NotificationClient {

	private final RestClient restClient;

	public NotificationClient(@Qualifier("notificationServiceRestClient") RestClient restClient) {
		this.restClient = restClient;
	}

	public void sendEmail(EmailTemplate template, String recipientEmail, String subject, Map<String, String> params) {
		try {
			EmailContent emailContent = new EmailContent(template, recipientEmail, subject, params);

			NotificationRequest request = new NotificationRequest(
				null,
				recipientEmail,
				null,
				subject,
				emailContent.getBody(),
				List.of(Channel.EMAIL)
			);

			restClient.post()
				.uri("/notifications")
				.body(request)
				.retrieve()
				.toBodilessEntity();

			log.info("Email sent successfully to {} (template: {})", recipientEmail, template.name());
		} catch (RestClientException e) {
			log.error("Failed to send email to {}", recipientEmail, e);
		} catch (Exception e) {
			log.error("Unexpected error sending email to {}", recipientEmail, e);
		}
	}

	public void sendWelcomeEmail(String userName, String email, String appUrl) {
		Map<String, String> params = new HashMap<>();
		params.put("userName", userName);
		params.put("appUrl", appUrl);

		sendEmail(
			EmailTemplate.WELCOME_REGISTRATION,
			email,
			"Добро пожаловать в TaskMaster!",
			params
		);
	}

	public void sendEmailVerification(String userName, String email, String verificationUrl) {
		Map<String, String> params = new HashMap<>();
		params.put("userName", userName);
		params.put("verificationUrl", verificationUrl);

		sendEmail(
			EmailTemplate.EMAIL_VERIFICATION,
			email,
			"Подтверждение электронной почты - TaskMaster",
			params
		);
	}

	public void sendInvitationEmail(
		String invitedUserName,
		String invitedEmail,
		String invitedByName,
		String organizationName,
		String role,
		String message,
		String expiresAt,
		String acceptUrl,
		String declineUrl
	) {
		Map<String, String> params = new HashMap<>();
		params.put("invitedUserName", invitedUserName);
		params.put("invitedByName", invitedByName);
		params.put("organizationName", organizationName);
		params.put("role", role);
		params.put("message", message != null ? message : "");
		params.put("expiresAt", expiresAt);
		params.put("acceptUrl", acceptUrl);
		params.put("declineUrl", declineUrl);

		sendEmail(
			EmailTemplate.INVITATION_EMPLOYEE,
			invitedEmail,
			"Приглашение в " + organizationName + " - TaskMaster",
			params
		);
	}
}
