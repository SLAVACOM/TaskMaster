package com.slavacom.auth_service.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EmailContent {
    private final EmailTemplate template;
    private final String recipientEmail;
    private final String subject;
    private final Map<String, String> params;

    public EmailContent(EmailTemplate template, String recipientEmail, String subject, Map<String, String> params) {
        this.template = template;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.params = params != null ? params : new HashMap<>();
    }

    public String getBody() {
        return switch (template) {
            case WELCOME_REGISTRATION -> getWelcomeRegistrationBody();
            case EMAIL_VERIFICATION -> getEmailVerificationBody();
            case INVITATION_EMPLOYEE -> getInvitationEmployeeBody();
        };
    }

    private String getWelcomeRegistrationBody() {
        String userName = params.getOrDefault("userName", "");
        String appUrl = params.getOrDefault("appUrl", "");
        return "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif;\">\n" +
            "    <h1>Добро пожаловать на TaskMaster! 👋</h1>\n" +
            "    <p>Привет, " + userName + "!</p>\n" +
            "    <p>Спасибо что зарегистрировались в нашем приложении для управления задачами.</p>\n" +
            "\n" +
            "    <h2>Что дальше?</h2>\n" +
            "    <ul>\n" +
            "        <li>Создайте вашу первую организацию</li>\n" +
            "        <li>Пригласите команду</li>\n" +
            "        <li>Начните управлять задачами</li>\n" +
            "    </ul>\n" +
            "\n" +
            "    <p><a href=\"" + appUrl + "/dashboard\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">\n" +
            "        Перейти в приложение\n" +
            "    </a></p>\n" +
            "\n" +
            "    <p style=\"color: #666; font-size: 12px;\">\n" +
            "        С уважением,<br/>\n" +
            "        Команда TaskMaster\n" +
            "    </p>\n" +
            "</body>\n" +
            "</html>";
    }

    private String getEmailVerificationBody() {
        String userName = params.getOrDefault("userName", "");
        String verificationUrl = params.getOrDefault("verificationUrl", "");
        return "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif;\">\n" +
            "    <h1>Подтверждение электронной почты 📧</h1>\n" +
            "    <p>Привет, " + userName + "!</p>\n" +
            "    <p>Спасибо за регистрацию. Пожалуйста, подтвердите вашу электронную почту, нажав на кнопку ниже:</p>\n" +
            "\n" +
            "    <p style=\"margin: 30px 0;\">\n" +
            "        <a href=\"" + verificationUrl + "\" style=\"background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;\">\n" +
            "            Подтвердить почту\n" +
            "        </a>\n" +
            "    </p>\n" +
            "\n" +
            "    <p style=\"color: #666; font-size: 12px;\">\n" +
            "        Эта ссылка действительна в течение 24 часов.<br/>\n" +
            "        Если это были не вы, просто игнорируйте это письмо.\n" +
            "    </p>\n" +
            "\n" +
            "    <p style=\"color: #999; font-size: 11px;\">\n" +
            "        С уважением,<br/>\n" +
            "        Команда TaskMaster\n" +
            "    </p>\n" +
            "</body>\n" +
            "</html>";
    }

    private String getInvitationEmployeeBody() {
        String invitedUserName = params.getOrDefault("invitedUserName", "");
        String invitedByName = params.getOrDefault("invitedByName", "");
        String organizationName = params.getOrDefault("organizationName", "");
        String role = params.getOrDefault("role", "");
        String message = params.getOrDefault("message", "");
        String expiresAt = params.getOrDefault("expiresAt", "");
        String acceptUrl = params.getOrDefault("acceptUrl", "");
        String declineUrl = params.getOrDefault("declineUrl", "");

        return "<html>\n" +
            "<body style=\"font-family: Arial, sans-serif;\">\n" +
            "    <h1>Приглашение в организацию 🎉</h1>\n" +
            "    <p>Привет, " + invitedUserName + "!</p>\n" +
            "    <p><strong>" + invitedByName + "</strong> приглашает вас присоединиться к организации <strong>" + organizationName + "</strong></p>\n" +
            "\n" +
            "    <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">\n" +
            "        <p><strong>Роль:</strong> " + role + "</p>\n" +
            "        <p><strong>Сообщение:</strong> " + message + "</p>\n" +
            "        <p><strong>Приглашение действительно до:</strong> " + expiresAt + "</p>\n" +
            "    </div>\n" +
            "\n" +
            "    <p>\n" +
            "        <a href=\"" + acceptUrl + "\" style=\"background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-right: 10px;\">\n" +
            "            ✅ Принять приглашение\n" +
            "        </a>\n" +
            "        <a href=\"" + declineUrl + "\" style=\"background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">\n" +
            "            ❌ Отклонить\n" +
            "        </a>\n" +
            "    </p>\n" +
            "\n" +
            "    <p style=\"color: #666; font-size: 12px; margin-top: 30px;\">\n" +
            "        С уважением,<br/>\n" +
            "        Команда TaskMaster\n" +
            "    </p>\n" +
            "</body>\n" +
            "</html>";
    }
}
