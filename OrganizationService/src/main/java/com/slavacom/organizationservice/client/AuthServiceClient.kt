package com.slavacom.organizationservice.client

import com.slavacom.organizationservice.dto.UpdateProfileRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.util.UUID

@Component
class AuthServiceClient(
    @Qualifier("authServiceRestClient")
    private val restClient: RestClient
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Update user's latest profile in AuthService
     * This triggers JWT regeneration to include profileId
     */
    fun updateProfile(userId: UUID, profileId: UUID, organizationId: UUID) {
        return try {
            val request = UpdateProfileRequest(
                userId = userId,
                profileId = profileId,
                organizationId = organizationId
            )

            logger.info(
                "Updating profile in AuthService: userId={}, profileId={}, organizationId={}",
                userId,
                profileId,
                organizationId
            )

            restClient.put()
                .uri("/api/auth/users/{userId}/profile", userId)
                .body(request)
                .retrieve()
                .onStatus(HttpStatus::isError) { httpRequest, response ->
                    logger.error(
                        "Failed to update profile in AuthService: status={}, body={}",
                        response.statusCode,
                        response.bodyAsString
                    )
                    throw AuthServiceException(
                        "Failed to update profile: ${response.statusCode}",
                        response.statusCode
                    )
                }
                .toBodilessEntity()

            logger.info("Successfully updated profile in AuthService for userId={}", userId)

        } catch (e: RestClientException) {
            logger.error("RestClient error while updating profile in AuthService: {}", e.message, e)
            throw AuthServiceException(
                "Error calling AuthService: ${e.message}",
                HttpStatus.SERVICE_UNAVAILABLE,
                e
            )
        } catch (e: AuthServiceException) {
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error while updating profile in AuthService: {}", e.message, e)
            throw AuthServiceException(
                "Unexpected error: ${e.message}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                e
            )
        }
    }

    /**
     * Get user's current profile from AuthService
     */
    fun getUserProfile(userId: UUID): String? {
        return try {
            logger.info("Getting user profile from AuthService: userId={}", userId)

            val response = restClient.get()
                .uri("/api/auth/users/{userId}", userId)
                .retrieve()
                .onStatus(HttpStatus::isError) { request, response ->
                    if (response.statusCode == HttpStatus.NOT_FOUND) {
                        logger.warn("User not found in AuthService: userId={}", userId)
                        return@onStatus
                    }
                    logger.error("Failed to get user profile: status={}", response.statusCode)
                    throw AuthServiceException(
                        "Failed to get user profile: ${response.statusCode}",
                        response.statusCode
                    )
                }
                .body(String::class.java)

            response?.let { logger.info("Retrieved user profile from AuthService") }
            response

        } catch (e: RestClientException) {
            logger.error("RestClient error while getting user profile: {}", e.message, e)
            throw AuthServiceException(
                "Error calling AuthService: ${e.message}",
                HttpStatus.SERVICE_UNAVAILABLE,
                e
            )
        }
    }
}

/**
 * Custom exception for AuthService errors
 */
class AuthServiceException(
    message: String,
    val statusCode: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
