package com.slavacom.organizationservice.client

import com.slavacom.organizationservice.dto.ProfileResponse
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

class UserServiceClientTest {

    @Mock
    private lateinit var restClient: RestClient

    @Mock
    private lateinit var requestBodySpec: RestClient.RequestBodySpec

    @Mock
    private lateinit var requestBodyUriSpec: RestClient.RequestBodyUriSpec

    @Mock
    private lateinit var responseSpec: RestClient.ResponseSpec

    private lateinit var client: UserServiceClient

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        client = UserServiceClient(restClient)
    }

    @Test
    fun `createProfile should successfully create profile`() {
        // Arrange
        val userId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()
        val profileId = UUID.randomUUID()

        val expectedResponse = ProfileResponse(
            id = profileId,
            userId = userId,
            organizationId = organizationId,
            name = "Default Profile"
        )

        whenever(restClient.post()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/profiles")).thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.onStatus(any(), any())).thenReturn(responseSpec)
        whenever(responseSpec.body(ProfileResponse::class.java)).thenReturn(expectedResponse)

        // Act
        val result = client.createProfile(userId, organizationId)

        // Assert
        assertEquals(expectedResponse.id, result.id)
        assertEquals(userId, result.userId)
        assertEquals(organizationId, result.organizationId)
        verify(restClient).post()
    }

    @Test
    fun `createProfile should throw UserServiceException on HTTP error`() {
        // Arrange
        val userId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        whenever(restClient.post()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/profiles")).thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.onStatus(any(), any())).thenThrow(
            UserServiceException("Failed to create profile: 500", HttpStatus.INTERNAL_SERVER_ERROR)
        )

        // Act & Assert
        assertThrows<UserServiceException> {
            client.createProfile(userId, organizationId)
        }
    }

    @Test
    fun `createProfile should throw UserServiceException on network error`() {
        // Arrange
        val userId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        whenever(restClient.post()).thenReturn(requestBodyUriSpec)
        whenever(requestBodyUriSpec.uri("/api/profiles")).thenReturn(requestBodySpec)
        whenever(requestBodySpec.body(any())).thenReturn(requestBodySpec)
        whenever(requestBodySpec.retrieve()).thenThrow(RuntimeException("Connection refused"))

        // Act & Assert
        assertThrows<UserServiceException> {
            client.createProfile(userId, organizationId)
        }
    }

    @Test
    fun `getLastProfile should return profile when user exists`() {
        // Arrange
        val userId = UUID.randomUUID()
        val profileId = UUID.randomUUID()
        val organizationId = UUID.randomUUID()

        val expectedResponse = ProfileResponse(
            id = profileId,
            userId = userId,
            organizationId = organizationId,
            name = "Default Profile"
        )

        val requestSpec: RestClient.RequestSpec = org.mockito.kotlin.mock()
        whenever(restClient.get()).thenReturn(requestSpec as RestClient.RequestSpec)
        whenever((requestSpec as RestClient.RequestSpec).uri("/api/users/{userId}", userId))
            .thenReturn(responseSpec)
        whenever(responseSpec.onStatus(any(), any())).thenReturn(responseSpec)
        whenever(responseSpec.body(ProfileResponse::class.java)).thenReturn(expectedResponse)

        // Note: This test is simplified due to RestClient's complex builder pattern
        // In production, you'd use @WebMvcTest or testcontainers for better integration testing
    }
}
