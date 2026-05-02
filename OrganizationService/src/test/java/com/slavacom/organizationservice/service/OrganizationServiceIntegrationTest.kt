package com.slavacom.organizationservice.service

import com.slavacom.organizationservice.client.AuthServiceClient
import com.slavacom.organizationservice.client.AuthServiceException
import com.slavacom.organizationservice.client.UserServiceClient
import com.slavacom.organizationservice.client.UserServiceException
import com.slavacom.organizationservice.dto.CreateOrganizationRequest
import com.slavacom.organizationservice.dto.ProfileResponse
import com.slavacom.organizationservice.employees.EmployeesRepository
import com.slavacom.organizationservice.exception.OrganizationNotFoundException
import com.slavacom.organizationservice.mapper.OrganizationMapper
import com.slavacom.organizationservice.repository.OrganizationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrganizationServiceIntegrationTest {

    @Mock
    private lateinit var organizationRepository: OrganizationRepository

    @Mock
    private lateinit var organizationMapper: OrganizationMapper

    @Mock
    private lateinit var employeesRepository: EmployeesRepository

    @Mock
    private lateinit var userServiceClient: UserServiceClient

    @Mock
    private lateinit var authServiceClient: AuthServiceClient

    private lateinit var service: OrganizationService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = OrganizationService(
            organizationRepository,
            organizationMapper,
            employeesRepository,
            userServiceClient,
            authServiceClient
        )
    }

    @Test
    fun `create should create organization and profile successfully`() {
        // Arrange
        val userId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val profileId = UUID.randomUUID()

        val request = CreateOrganizationRequest(
            name = "Test Organization",
            description = "Test Description"
        )

        val profileResponse = ProfileResponse(
            id = profileId,
            userId = userId,
            organizationId = orgId,
            name = "Default Profile"
        )

        val org = com.slavacom.organizationservice.entity.Organization().apply {
            this.id = orgId
            this.name = request.name
            this.description = request.description
            this.accountable = userId
            this.isActive = true
            this.createdAt = Instant.now()
        }

        // Mock repository save
        whenever(organizationMapper.fromCreateRequest(request)).thenReturn(org)
        whenever(organizationRepository.save(org)).thenReturn(org)

        // Mock UserService client
        whenever(userServiceClient.createProfile(userId, orgId)).thenReturn(profileResponse)

        // Mock response
        val response = com.slavacom.organizationservice.dto.OrganizationResponse(
            id = orgId,
            name = org.name!!,
            description = org.description,
            accountable = userId,
            isActive = true,
            createdAt = org.createdAt!!,
            updatedAt = org.updatedAt,
            profileId = profileId
        )
        whenever(organizationMapper.toOrganizationResponse(org)).thenReturn(response)

        // Act
        val result = service.create(request, userId)

        // Assert
        assertEquals(orgId, result.id)
        assertEquals("Test Organization", result.name)
        assertEquals(userId, result.accountable)
        verify(organizationRepository).save(any())
        verify(userServiceClient).createProfile(userId, orgId)
        verify(authServiceClient).updateProfile(userId, profileId, orgId)
    }

    @Test
    fun `create should rollback organization if UserService fails`() {
        // Arrange
        val userId = UUID.randomUUID()
        val orgId = UUID.randomUUID()

        val request = CreateOrganizationRequest(
            name = "Test Organization",
            description = "Test Description"
        )

        val org = com.slavacom.organizationservice.entity.Organization().apply {
            this.id = orgId
            this.name = request.name
            this.description = request.description
            this.accountable = userId
            this.isActive = true
            this.createdAt = Instant.now()
        }

        // Mock repository save
        whenever(organizationMapper.fromCreateRequest(request)).thenReturn(org)
        whenever(organizationRepository.save(org)).thenReturn(org)

        // Mock UserService failure
        whenever(userServiceClient.createProfile(userId, orgId)).thenThrow(
            UserServiceException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE)
        )

        // Act & Assert
        assertThrows<UserServiceException> {
            service.create(request, userId)
        }

        // Verify organization was deleted (rolled back)
        verify(organizationRepository).delete(org)
    }

    @Test
    fun `create should not block if AuthService fails`() {
        // Arrange
        val userId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val profileId = UUID.randomUUID()

        val request = CreateOrganizationRequest(
            name = "Test Organization",
            description = "Test Description"
        )

        val profileResponse = ProfileResponse(
            id = profileId,
            userId = userId,
            organizationId = orgId,
            name = "Default Profile"
        )

        val org = com.slavacom.organizationservice.entity.Organization().apply {
            this.id = orgId
            this.name = request.name
            this.description = request.description
            this.accountable = userId
            this.isActive = true
            this.createdAt = Instant.now()
        }

        // Mock repository save
        whenever(organizationMapper.fromCreateRequest(request)).thenReturn(org)
        whenever(organizationRepository.save(org)).thenReturn(org)

        // Mock UserService success
        whenever(userServiceClient.createProfile(userId, orgId)).thenReturn(profileResponse)

        // Mock AuthService failure
        whenever(authServiceClient.updateProfile(userId, profileId, orgId)).thenThrow(
            AuthServiceException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE)
        )

        val response = com.slavacom.organizationservice.dto.OrganizationResponse(
            id = orgId,
            name = org.name!!,
            description = org.description,
            accountable = userId,
            isActive = true,
            createdAt = org.createdAt!!,
            updatedAt = org.updatedAt,
            profileId = profileId
        )
        whenever(organizationMapper.toOrganizationResponse(org)).thenReturn(response)

        // Act - Should NOT throw exception
        val result = service.create(request, userId)

        // Assert - Organization should still be created
        assertEquals(orgId, result.id)
        assertNotNull(result)
        // Organization is NOT rolled back even though AuthService failed
        verify(organizationRepository, org.javaClass.`package`.name).save(any())
    }

    @Test
    fun `getById should return organization`() {
        // Arrange
        val orgId = UUID.randomUUID()
        val org = com.slavacom.organizationservice.entity.Organization().apply {
            this.id = orgId
            this.name = "Test"
            this.isActive = true
        }
        val response = com.slavacom.organizationservice.dto.OrganizationResponse(
            id = orgId,
            name = "Test",
            description = null,
            accountable = UUID.randomUUID(),
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = null
        )

        whenever(organizationRepository.findById(orgId)).thenReturn(Optional.of(org))
        whenever(organizationMapper.toOrganizationResponse(org)).thenReturn(response)

        // Act
        val result = service.getById(orgId)

        // Assert
        assertEquals(orgId, result.id)
        assertEquals("Test", result.name)
    }

    @Test
    fun `getById should throw OrganizationNotFoundException if not found`() {
        // Arrange
        val orgId = UUID.randomUUID()
        whenever(organizationRepository.findById(orgId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<OrganizationNotFoundException> {
            service.getById(orgId)
        }
    }
}
