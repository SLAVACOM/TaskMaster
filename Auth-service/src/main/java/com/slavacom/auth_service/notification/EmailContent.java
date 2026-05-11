package com.slavacom.auth_service.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
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
        return """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h1>Добро пожаловать на TaskMaster! 👋</h1>
                <p>Привет, """ + params.get("userName") + """!</p>
                <p>Спасибо что зарегистрировались в нашем приложении для управления задачами.</p>

                <h2>Что дальше?</h2>
                <ul>
                    <li>Создайте вашу первую организацию</li>
                    <li>Пригласите команду</li>
                    <li>Начните управлять задачами</li>
                </ul>

                <p><a href=""" + params.get("appUrl") + """/dashboard" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                    Перейти в приложение
                </a></p>

                <p style="color: #666; font-size: 12px;">
                    С уважением,<br/>
                    Команда TaskMaster
                </p>
            </body>
            </html>
            """;
    }

    private String getEmailVerificationBody() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h1>Подтверждение электронной почты 📧</h1>
                <p>Привет, """ + params.get("userName") + """!</p>
                <p>Спасибо за регистрацию. Пожалуйста, подтвердите вашу электронную почту, нажав на кнопку ниже:</p>

                <p style="margin: 30px 0;">
                    <a href=""" + params.get("verificationUrl") + """" style="background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;">
                        Подтвердить почту
                    </a>
                </p>

                <p style="color: #666; font-size: 12px;">
                    Эта ссылка действительна в течение 24 часов.<br/>
                    Если это были не вы, просто игнорируйте это письмо.
                </p>

                <p style="color: #999; font-size: 11px;">
                    С уважением,<br/>
                    Команда TaskMaster
                </p>
            </body>
            </html>
            """;
    }

    private String getInvitationEmployeeBody() {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h1>Приглашение в организацию 🎉</h1>
                <p>Привет, """ + params.get("invitedUserName") + """!</p>
                <p><strong>""" + params.get("invitedByName") + """</strong> приглашает вас присоединиться к организации <strong>""" + params.get("organizationName") + """</strong></p>

                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                    <p><strong>Роль:</strong> """ + params.get("role") + """</p>
                    <p><strong>Сообщение:</strong> """ + params.get("message") + """</p>
                    <p><strong>Приглашение действительно до:</strong> """ + params.get("expiresAt") + """</p>
                </div>

                <p>
                    <a href=""" + params.get("acceptUrl") + """" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-right: 10px;">
                        ✅ Принять приглашение
                    </a>
                    <a href=""" + params.get("declineUrl") + """" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        ❌ Отклонить
                    </a>
                </p>

                <p style="color: #666; font-size: 12px; margin-top: 30px;">
                    С уважением,<br/>
                    Команда TaskMaster
                </p>
            </body>
            </html>
            """;
    }
}
