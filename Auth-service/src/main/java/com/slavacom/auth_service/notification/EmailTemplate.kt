package com.slavacom.auth_service.notification

enum class EmailTemplate {
    WELCOME_REGISTRATION,
    EMAIL_VERIFICATION,
    INVITATION_EMPLOYEE
}

data class EmailContent(
    val template: EmailTemplate,
    val recipientEmail: String,
    val subject: String,
    val params: Map<String, String> = emptyMap()
) {
    fun getBody(): String = when (template) {
        EmailTemplate.WELCOME_REGISTRATION -> {
            """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1>Добро пожаловать на TaskMaster! 👋</h1>
                    <p>Привет, ${params["userName"]}!</p>
                    <p>Спасибо что зарегистрировались в нашем приложении для управления задачами.</p>

                    <h2>Что дальше?</h2>
                    <ul>
                        <li>Создайте вашу первую организацию</li>
                        <li>Пригласите команду</li>
                        <li>Начните управлять задачами</li>
                    </ul>

                    <p><a href="${params["appUrl"]}/dashboard" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                        Перейти в приложение
                    </a></p>

                    <p style="color: #666; font-size: 12px;">
                        С уважением,<br/>
                        Команда TaskMaster
                    </p>
                </body>
                </html>
            """.trimIndent()
        }

        EmailTemplate.EMAIL_VERIFICATION -> {
            """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1>Подтверждение электронной почты 📧</h1>
                    <p>Привет, ${params["userName"]}!</p>
                    <p>Спасибо за регистрацию. Пожалуйста, подтвердите вашу электронную почту, нажав на кнопку ниже:</p>

                    <p style="margin: 30px 0;">
                        <a href="${params["verificationUrl"]}" style="background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-size: 16px;">
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
            """.trimIndent()
        }

        EmailTemplate.INVITATION_EMPLOYEE -> {
            """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1>Приглашение в организацию 🎉</h1>
                    <p>Привет, ${params["invitedUserName"]}!</p>
                    <p><strong>${params["invitedByName"]}</strong> приглашает вас присоединиться к организации <strong>${params["organizationName"]}</strong></p>

                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Роль:</strong> ${params["role"]}</p>
                        <p><strong>Сообщение:</strong> ${params["message"]}</p>
                        <p><strong>Приглашение действительно до:</strong> ${params["expiresAt"]}</p>
                    </div>

                    <p>
                        <a href="${params["acceptUrl"]}" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-right: 10px;">
                            ✅ Принять приглашение
                        </a>
                        <a href="${params["declineUrl"]}" style="background-color: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
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
    }
}
