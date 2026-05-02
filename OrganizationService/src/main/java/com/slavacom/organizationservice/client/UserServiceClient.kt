package com.slavacom.organizationservice.client

import com.slavacom.organizationservice.dto.CreateProfileRequest
import com.slavacom.organizationservice.dto.ProfileResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.util.UUID

@Component
class UserServiceClient(
    @Qualifier("userServiceRestClient")
    private val restClient: RestClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Create a profile for a user in a specific organization
     */
    fun createProfile(userId: UUID, organizationId: UUID): ProfileResponse {
        return try {
            val request = CreateProfileRequest(
                userId = userId,
                organizationId = organizationId,
                name = "Default Profile"
            )

            logger.info("Creating profile for userId={} in organizationId={}", userId, organizationId)

            val response = restClient.post()
                .uri("/api/profiles")
                .body(request)
                .retrieve()
                .onStatus(HttpStatus::isError) { request, response ->
                    logger.error(
                        "Failed to create profile: status={}, body={}",
                        response.statusCode,
                        response.bodyAsString
                    )
                    throw UserServiceException(
                        "Failed to create profile: ${response.statusCode}",
                        response.statusCode
                    )
                }
                .body(ProfileResponse::class.java)
                ?: throw UserServiceException("Empty response from UserService", HttpStatus.INTERNAL_SERVER_ERROR)

            logger.info("Successfully created profile: profileId={}", response.id)
            response

        } catch (e: RestClientException) {
            logger.error("RestClient error while creating profile: {}", e.message, e)
            throw UserServiceException("Error calling UserService: ${e.message}", HttpStatus.SERVICE_UNAVAILABLE, e)
        } catch (e: Exception) {
            logger.error("Unexpected error while creating profile: {}", e.message, e)
            throw UserServiceException("Unexpected error: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    /**
     * Get user's last profile
     */
    fun getLastProfile(userId: UUID): ProfileResponse? {
        return try {
            logger.info("Getting last profile for userId={}", userId)

            val response = restClient.get()
                .uri("/api/users/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatus::isError) { request, response ->
                    if (response.statusCode == HttpStatus.NOT_FOUND) {
                        logger.warn("User not found: userId={}", userId)
                        return@onStatus
                    }
                    logger.error("Failed to get user: status={}", response.statusCode)
                    throw UserServiceException("Failed to get user: ${response.statusCode}", response.statusCode)
                }
                .body(ProfileResponse::class.java)

            response?.let { logger.info("Got profile: profileId={}", it.id) }
            response

        } catch (e: RestClientException) {
            logger.error("RestClient error while getting profile: {}", e.message, e)
            throw UserServiceException("Error calling UserService: ${e.message}", HttpStatus.SERVICE_UNAVAILABLE, e)
        }
    }
}

/**
 * Custom exception for UserService errors
 */
class UserServiceException(
    message: String,
    val statusCode: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
