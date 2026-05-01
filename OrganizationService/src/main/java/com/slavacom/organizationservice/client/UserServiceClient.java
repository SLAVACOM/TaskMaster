//package com.slavacom.organizationservice.client;
//
//import com.slavacom.organizationservice.dto.;
//import com.slavacom.organizationservice.dto.
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClient;
//import org.springframework.web.client.RestClientException;
//
//import java.util.UUID;
//
///**
// * Клиент для взаимодействия с User Service
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class UserServiceClient {
//
//    @Value("${user-service.url:http://localhost:8082}")
//    private String userServiceUrl;
//
//    private final RestClient restClient;
//
//    /**
//     * Создание нового профиля в User Service
//     *
//     * @param userId ID пользователя
//     * @param organizationId ID организации
//     * @return созданный профиль
//     */
//    public ProfileResponse createProfile(UUID userId, UUID organizationId) {
//        try {
//            log.info("Creating profile in User Service for userId: {} in organization: {}", userId, organizationId);
//
//            CreateProfileRequest request = CreateProfileRequest.builder()
//                    .userId(userId)
//                    .organizationId(organizationId)
//                    .name("Default Profile")
//                    .description("Auto-created profile for organization")
//                    .build();
//
//            ProfileResponse response = restClient.post()
//                    .uri(userServiceUrl + "/api/profiles")
//                    .body(request)
//                    .retrieve()
//                    .body(ProfileResponse.class);
//
//            if (response != null) {
//                log.info("Profile created successfully with ID: {}", response.getId());
//                // Добавляем статус успешного создания
//                response.setName(response.getName() != null ? response.getName() : "Default Profile");
//                response.setDescription(response.getDescription() != null ? response.getDescription() : "Profile created in User Service");
//                return response;
//            } else {
//                log.error("Failed to create profile - no response from User Service");
//            }
//
//        } catch (Exception e) {
//            log.error("Error connecting to User Service at {}: {}", userServiceUrl, e.getMessage());
//            log.warn("User Service is unavailable. Creating mock profile as fallback.");
//        }
//
//        // Fallback: создаем фиктивный профиль если User Service недоступен
//        ProfileResponse mockProfile = ProfileResponse.builder()
//                .id(UUID.randomUUID())
//                .userId(userId)
//                .organizationId(organizationId)
//                .name("Default Profile (Mock)")
//                .description("Fallback profile - User Service unavailable at " + userServiceUrl)
//                .isActive(true)
//                .build();
//
//        log.info("Mock profile created with ID: {}", mockProfile.getId());
//        return mockProfile;
//    }
//
//    /**
//     * Обновление последнего профиля пользователя
//     *
//     * @param userId ID пользователя
//     * @param profileId ID нового последнего профиля
//     */
//    public void updateUserLastProfile(UUID userId, UUID profileId) {
//        try {
//            log.info("Updating last profile for userId: {} to profileId: {}", userId, profileId);
//
//            restClient.put()
//                    .uri(userServiceUrl + "/api/users/{userId}/last-profile/{profileId}", userId, profileId)
//                    .retrieve()
//                    .toBodilessEntity();
//
//            log.info("Last profile updated successfully for userId: {}", userId);
//        } catch (Exception e) {
//            log.error("Error connecting to User Service at {}: {}", userServiceUrl, e.getMessage());
//            log.warn("User Service is unavailable. Skipping last profile update for userId: {}", userId);
//            // Не прерываем выполнение, просто логируем предупреждение
//        }
//    }
//
//    /**
//     * Проверка доступности User Service
//     *
//     * @return true если User Service доступен
//     */
//    public boolean isUserServiceAvailable() {
//        try {
//            log.debug("Checking User Service availability at: {}", userServiceUrl);
//
//            // Простая проверка - делаем HEAD запрос
//            restClient.head()
//                    .uri(userServiceUrl + "/actuator/health")
//                    .retrieve()
//                    .toBodilessEntity();
//
//            log.debug("User Service is available");
//            return true;
//        } catch (Exception e) {
//            log.debug("User Service is unavailable: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * Найти ID пользователя по username или email.
//     */
//    public UUID findUserIdByIdentifier(String identifier) {
//        UUID byLogin = getUserIdByLogin(identifier);
//        if (byLogin != null) {
//            return byLogin;
//        }
//        return getUserIdByEmail(identifier);
//    }
//
//    public UUID getUserIdByLogin(String login) {
//        try {
//            return restClient.post()
//                    .uri(userServiceUrl + "/api/users/findUser/login/{login}", login)
//                    .retrieve()
//                    .body(UUID.class);
//        } catch (RestClientException e) {
//            log.debug("User not found by username {}", login);
//            return null;
//        }
//    }
//
//    public UUID getUserIdByEmail(String email) {
//        try {
//            return restClient.post()
//                    .uri(userServiceUrl + "/api/users/findUser/email/{email}", email)
//                    .retrieve()
//                    .body(UUID.class);
//        } catch (RestClientException e) {
//            log.debug("User not found by email {}", email);
//            return null;
//        }
//    }
//}
