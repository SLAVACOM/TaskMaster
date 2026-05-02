package com.slavacom.organizationservice.service

import com.slavacom.organizationservice.client.AuthServiceClient
import com.slavacom.organizationservice.client.AuthServiceException
import com.slavacom.organizationservice.client.UserServiceClient
import com.slavacom.organizationservice.client.UserServiceException
import com.slavacom.organizationservice.dto.CreateOrganizationRequest
import com.slavacom.organizationservice.dto.OrganizationResponse
import com.slavacom.organizationservice.dto.UpdateOrganizationRequest
import com.slavacom.organizationservice.dto.UserOrganizationInfoResponse
import com.slavacom.organizationservice.employees.EmployeesRepository
import com.slavacom.organizationservice.exception.OrganizationNotFoundException
import com.slavacom.organizationservice.mapper.OrganizationMapper
import com.slavacom.organizationservice.repository.OrganizationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationMapper: OrganizationMapper,
    private val employeesRepository: EmployeesRepository,
    private val userServiceClient: UserServiceClient,
    private val authServiceClient: AuthServiceClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAll(isActive: Boolean = true): List<OrganizationResponse> {
        val data = if (isActive) organizationRepository.findAllByIsActiveTrue()
                   else organizationRepository.findAll()
        return data.map { organizationMapper.toOrganizationResponse(it) }
    }

    fun getById(id: UUID): OrganizationResponse {
        val organization = organizationRepository.findById(id)
            .orElseThrow { OrganizationNotFoundException(id) }
        return organizationMapper.toOrganizationResponse(organization)
    }

    @Transactional
    fun create(request: CreateOrganizationRequest, accountable: UUID): OrganizationResponse {
        logger.info("Creating organization: name={}, accountable={}", request.name, accountable)

        // Step 1: Create organization in local database
        val org = organizationMapper.fromCreateRequest(request)
        org.accountable = accountable
        val savedOrg = organizationRepository.save(org)
        logger.info("Organization created in DB: orgId={}", savedOrg.id)

        return try {
            // Step 2: Create profile in UserService
            logger.info("Creating profile in UserService for userId={} in orgId={}", accountable, savedOrg.id)
            val profileResponse = userServiceClient.createProfile(accountable, savedOrg.id!!)
            logger.info("Profile created: profileId={}", profileResponse.id)

            // Step 3: Update profile in AuthService (JWT will include profileId on next refresh)
            logger.info("Updating profile in AuthService: userId={}, profileId={}", accountable, profileResponse.id)
            try {
                authServiceClient.updateProfile(accountable, profileResponse.id, savedOrg.id!!)
                logger.info("Profile updated in AuthService for userId={}", accountable)
            } catch (e: AuthServiceException) {
                logger.warn(
                    "Failed to update profile in AuthService (non-blocking): {}",
                    e.message
                )
                // Don't block organization creation if AuthService fails
                // User will get updated JWT on next login/refresh
            }

            // Return response with profileId
            val response = organizationMapper.toOrganizationResponse(savedOrg)
            response

        } catch (e: UserServiceException) {
            logger.error("Failed to create profile in UserService, rolling back organization creation: {}", e.message)
            // Delete the organization since profile creation failed
            organizationRepository.delete(savedOrg)
            logger.info("Organization rolled back: orgId={}", savedOrg.id)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error during organization creation, rolling back: {}", e.message, e)
            organizationRepository.delete(savedOrg)
            throw e
        }
    }

    fun update(id: UUID, request: UpdateOrganizationRequest): OrganizationResponse {
        val org = organizationRepository.findById(id)
            .orElseThrow { OrganizationNotFoundException(id) }
        request.name?.let { org.name = it }
        request.description?.let { org.description = it }
        request.isActive?.let { org.isActive = it }
        return organizationMapper.toOrganizationResponse(organizationRepository.save(org))
    }

    fun deactivate(id: UUID) {
        val org = organizationRepository.findById(id)
            .orElseThrow { OrganizationNotFoundException(id) }
        org.isActive = false
        organizationRepository.save(org)
    }

    fun getUserOrganizationInfo(userId: UUID): UserOrganizationInfoResponse? {
        val employee = employeesRepository.findByUserIdAndIsActiveTrue(userId).orElse(null) ?: return null
        val org = organizationRepository.findById(employee.organizationId!!)
            .orElseThrow { OrganizationNotFoundException(employee.organizationId!!) }
        return UserOrganizationInfoResponse(
            id = org.id.toString(),
            name = org.name!!,
            description = org.description,
            userId = org.accountable.toString(),
            currentUserId = userId.toString(),
            role = employee.role!!
        )
    }
}
