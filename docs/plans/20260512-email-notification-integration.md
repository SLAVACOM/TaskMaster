# Email Notification Integration for AuthService and OrganizationService

## Overview

Implemented email notifications for user registration (welcome email and email verification) and employee invitations. The system integrates with NotificationService via REST API to send HTML-formatted emails through SMTP/Telegram channels.

**Features:**
- Welcome email on registration
- Email verification letter with verification link
- Employee invitation emails with accept/decline buttons
- Non-blocking error handling (email failures don't stop registration/invitation)

## Architecture

### Email Templates (Auth-service)
- **EmailTemplate.kt**: Enum with three email types:
  - `WELCOME_REGISTRATION` - Welcome email with app link
  - `EMAIL_VERIFICATION` - Email verification with 24-hour token link
  - `INVITATION_EMPLOYEE` - Employee invitation with accept/decline buttons
  
- **EmailContent.kt**: Data class with `getBody()` method that generates HTML content based on template type and parameters

### NotificationClient Components

**Auth-service: NotificationClient.kt**
- Injects `notificationServiceRestClient` RestClient bean
- Methods:
  - `sendEmail(template, email, subject, params)` - Generic email sender
  - `sendWelcomeEmail(userName, email, appUrl)` - Welcome email
  - `sendEmailVerification(userName, email, verificationUrl)` - Verification email
  - `sendInvitationEmail(...)` - Invitation email

**OrganizationService: NotificationClient.kt**
- Injects `notificationServiceRestClient` RestClient bean
- Method:
  - `sendInvitationEmail(...)` - Sends HTML invitation email to invited user

### Integration Points

**Auth-service AuthService.register()**
- After successful user registration, calls:
  - `notificationClient.sendWelcomeEmail()` - with app URL
  - `notificationClient.sendEmailVerification()` - with verification URL
- Errors are logged but don't block registration

**OrganizationService InvitationService.invite()**
- After saving invitation, calls `sendInvitationEmailAsync()`
- Fetches user info (email, name) from UserService via REST
- Fetches organization info from OrganizationRepository
- Generates accept/decline URLs with invitation ID
- Calls `notificationClient.sendInvitationEmail()` with all parameters
- Errors are logged but don't block invitation creation

## Configuration

### RestClient Beans

**Auth-service RestClientConfig.java**
```java
@Bean("notificationServiceRestClient")
public RestClient notificationServiceRestClient(
    @Value("${services.notification-service.url:http://localhost:8090}")
    String notificationServiceUrl
) {
    return RestClient.builder()
        .baseUrl(notificationServiceUrl)
        .build();
}
```

**OrganizationService RestClientConfig.kt**
```kotlin
@Bean("notificationServiceRestClient")
fun notificationServiceRestClient(): RestClient {
    return RestClient.builder()
        .baseUrl(notificationServiceUrl)
        .build()
}
```

### Application Configuration

**Auth-service application.yml & application-docker.yml**
- Added `services.notification-service.url` property
- Added `app-url` property (default: http://localhost:3000)

**OrganizationService application.yml & application-docker.yml**
- Added `services.notification-service.url` property  
- Added `app-url` property (default: http://localhost:3000)
- Created docker configuration file

## Implementation Status

### Completed Tasks

✅ **Auth-service Email System**
- Created EmailTemplate.kt with three email templates
- Created EmailContent.kt with HTML body generation
- Created NotificationClient.kt component
- Updated AuthService to inject NotificationClient
- Added email sending to register() method
- Configured RestClient bean for NotificationService
- Updated application.yml with notification service URL and app-url

✅ **OrganizationService Invitation Emails**
- Created NotificationClient.kt for invitation emails
- Extended UserServiceClient with getUserInfo() method
- Created UserInfoDto for user information
- Updated InvitationService to send invitation emails
- Integrated email sending with invitation creation
- Configured RestClient bean for NotificationService
- Updated application.yml with notification service configuration

✅ **Configuration Files**
- Updated Auth-service application.yml and application-docker.yml
- Updated OrganizationService application.yml
- Created OrganizationService application-docker.yml
- Added notification service URLs to both services
- Added app-url configuration property

## Email Content

### Welcome Email (WELCOME_REGISTRATION)
- Greeting with user first name
- App link to dashboard
- Next steps list (create organization, invite team, manage tasks)
- Signature

### Email Verification (EMAIL_VERIFICATION)
- Greeting with user first name
- Verification button with 24-hour expiration notice
- Link to email verification endpoint
- Signature

### Employee Invitation (INVITATION_EMPLOYEE)
- Greeting with invited user first name
- Inviter name and organization name
- Role, custom message, expiration date
- Accept and Decline buttons with URLs
- Signature

## REST API Integration

**NotificationService Endpoint**
- `POST /notifications`
- Request body: `NotificationRequest` with email, title, message, channels (Channel.EMAIL)
- No response body expected
- Non-blocking - failures don't block caller

## Environment Configuration

### Local Development (.env or environment variables)
```
APP_URL=http://localhost:3000
NOTIFICATION_SERVICE_URL=http://localhost:8090
USER_SERVICE_URL=http://localhost:8082
```

### Docker Environment
```
APP_URL=http://localhost:3000 (or custom frontend URL)
NOTIFICATION_SERVICE_URL=http://notification-service:8090
USER_SERVICE_URL=http://user-service:8082
```

## Testing Scenarios

1. **Registration Flow**
   - Create new user via /auth/register
   - Verify welcome email sent to user email
   - Verify verification email sent with verification link

2. **Invitation Flow**
   - Create invitation via /invitations
   - Verify invitation email sent to invited user
   - Verify email contains accept/decline links
   - Test accept and decline buttons

3. **Error Handling**
   - Simulate NotificationService unavailability
   - Verify registration still completes (email error is non-blocking)
   - Verify invitation still created (email error is non-blocking)
   - Check logs for email sending errors

## Known Limitations

- Verification tokens are currently generated as random UUIDs (not stored/validated)
- Invitation URLs use invitation ID directly (no token validation yet)
- No email delivery confirmation or bounce handling
- Email templates are hardcoded (not externalized to templates)

## Future Enhancements

- Add email delivery status tracking
- Implement email template externalization (Thymeleaf, FreeMarker)
- Add email retry logic with exponential backoff
- Implement verification token storage and validation
- Add email rate limiting
- Support for multiple language templates
- Email preview/testing endpoint

## Files Modified/Created

### Auth-service
- `src/main/java/com/slavacom/auth_service/notification/EmailTemplate.kt` (existed, unchanged)
- `src/main/java/com/slavacom/auth_service/notification/EmailContent.kt` (existed, unchanged)
- `src/main/java/com/slavacom/auth_service/notification/NotificationClient.kt` (modified - added @Qualifier)
- `src/main/java/com/slavacom/auth_service/service/AuthService.java` (modified - added email sending)
- `src/main/java/com/slavacom/auth_service/config/RestClientConfig.java` (modified - added notificationServiceRestClient bean)
- `src/main/resources/application.yml` (modified - added notification service config)
- `src/main/resources/application-docker.yml` (modified - added notification service config)

### OrganizationService
- `src/main/java/com/slavacom/organizationservice/notification/NotificationClient.kt` (created)
- `src/main/java/com/slavacom/organizationservice/dto/UserInfoDto.kt` (created)
- `src/main/java/com/slavacom/organizationservice/invitation/InvitationService.kt` (modified - added email sending)
- `src/main/java/com/slavacom/organizationservice/client/UserServiceClient.kt` (modified - added getUserInfo method)
- `src/main/java/com/slavacom/organizationservice/config/RestClientConfig.kt` (modified - added notificationServiceRestClient bean)
- `src/main/resources/application.yml` (modified - added notification service config)
- `src/main/resources/application-docker.yml` (created)

## Endpoint Usage Examples

### Register User (Auth-service)
```
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePassword123"
}
```

Sends:
- Welcome email to user@example.com
- Verification email with link to /verify-email?token=<uuid>

### Create Invitation (OrganizationService)
```
POST /organizations/{orgId}/invitations
Content-Type: application/json

{
  "invitedUserId": "user-uuid",
  "identifier": "john@company.com",
  "role": "DEVELOPER",
  "message": "Please join our team!",
  "expiresAt": "2026-05-19T00:00:00Z"
}
```

Sends:
- Invitation email to invited user
- Links to /invitations/{invitationId}/accept and /decline
