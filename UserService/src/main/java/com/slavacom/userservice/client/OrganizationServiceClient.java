package com.slavacom.user_service.client;

import com.slavacom.user_service.dto.OrganizationInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Клиент для взаимодействия с Organization Service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationServiceClient {

    @Value("${organization-service.url:http://localhost:8083}")
    private String organizationServiceUrl;

    private final RestClient restClient;

    /**
     * Получить информацию об организации пользователя
     *
     * @param userId ID пользователя
     * @return информация об организации пользователя
     */
    public OrganizationInfoDto getUserOrganizationInfo(UUID userId) {
        try {
            log.info("Fetching organization info from Organization Service for userId: {}", userId);

            OrganizationInfoDto organizationInfo = restClient.get()
                    .uri(organizationServiceUrl + "/api/organizations/user/{userId}/info", userId)
                    .retrieve()
                    .body(OrganizationInfoDto.class);

            if (organizationInfo != null) {
                log.info("Organization info found for userId {}: {}", userId, organizationInfo.name());
            } else {
                log.info("No organization info found for userId: {}", userId);
            }

            return organizationInfo;
        } catch (RestClientException e) {
            log.warn("Error fetching organization info from Organization Service for userId: {}", userId, e);
            return null;
        }
    }
}

