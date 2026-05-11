package com.slavacom.userservice.client;

import com.slavacom.userservice.dto.OrganizationInfoDto;
import com.slavacom.userservice.exception.OrganizationNotFoundException;
import com.slavacom.userservice.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;


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

    /**
     * Проверить существование организации по ID
     *
     * @param organizationId ID организации
     * @throws OrganizationNotFoundException если организация не найдена (404)
     * @throws ServiceUnavailableException если сервис недоступен
     */
    public OrganizationInfoDto getOrganizationById(UUID organizationId) {
        try {
            log.debug("Fetching organization from Organization Service: {}", organizationId);

            var orgInfo = restClient.get()
                    .uri(organizationServiceUrl + "/api/organizations/{id}", organizationId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, responseException) -> {
                        if (responseException.getStatusCode().value() == 404) {
                            log.warn("Organization not found in Organization Service: {}", organizationId);
                            throw new OrganizationNotFoundException(organizationId.toString());
                        }
                    })
                    .body(OrganizationInfoDto.class);

            if (orgInfo != null) {
                log.info("Organization found: {} ({})", organizationId, orgInfo.name());
            }

            return orgInfo;
        } catch (OrganizationNotFoundException e) {
            throw e;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("Organization not found: {}", organizationId);
                throw new OrganizationNotFoundException(organizationId.toString());
            }
            log.error("Error response from Organization Service: {}", organizationId, e);
            throw new ServiceUnavailableException("OrganizationService", e);
        } catch (RestClientException e) {
            log.error("Failed to connect to Organization Service for organization: {}", organizationId, e);
            throw new ServiceUnavailableException("OrganizationService", e);
        }
    }
}

