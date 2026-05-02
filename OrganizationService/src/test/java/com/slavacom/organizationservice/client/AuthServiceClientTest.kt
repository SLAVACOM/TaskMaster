package com.slavacom.organizationservice.client

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import java.util.UUID
import kotlin.test.assertEquals

class AuthServiceClientTest {

    @Mock
    private lateinit var restClient: RestClient

    @Mock
    private lateinit var requestBodyUriSpec: RestClient.RequestBodyUriSpec

    @Mock
    private lateinit var requestBodySpec: RestClient.RequestBodySpec

    @Mock
    private lateinit var responseSpec: RestClient.ResponseSpec

    private lateinit var client: AuthServiceClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        client = AuthServiceClient(restClient)
    }

    @Test
    fun `updateProfile should successfully update profile`() {
        // Arrange
        val userId = UUID.randomUUID()
        val profileId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        whenever(restClient.put()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/auth/users/{userId}/profile", userId))
            .thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.onStatus(any(), any())).thenReturn(responseSpec)
        whenever(responseSpec.toBodilessEntity()).thenReturn(null)

        // Act
        client.updateProfile(userId, profileId, organizationId)

        // Assert
        verify(restClient).put()
    }

    @Test
    fun `updateProfile should throw AuthServiceException on HTTP error`() {
        // Arrange
        val userId = UUID.randomUUID()
        val profileId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        whenever(restClient.put()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/auth/users/{userId}/profile", userId))
            .thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.onStatus(any(), any())).thenThrow(
            AuthServiceException("Failed to update profile: 500", HttpStatus.INTERNAL_SERVER_ERROR)
        )

        // Act & Assert
        assertThrows<AuthServiceException> {
            client.updateProfile(userId, profileId, organizationId)
        }
    }

    @Test
    fun `updateProfile should throw AuthServiceException on network error`() {
        // Arrange
        val userId = UUID.randomUUID()
        val profileId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        whenever(restClient.put()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/auth/users/{userId}/profile", userId))
            .thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenThrow(RuntimeException("Connection refused"))

        // Act & Assert
        assertThrows<AuthServiceException> {
            client.updateProfile(userId, profileId, organizationId)
        }
    }
}
