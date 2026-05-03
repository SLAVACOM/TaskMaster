package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.client.AuthServiceClient
import com.slavacom.organizationservice.client.UserServiceClient
import com.slavacom.organizationservice.entity.EmployeeRole
import com.slavacom.organizationservice.exception.AuthServiceException
import com.slavacom.organizationservice.exception.EmployeeAlreadyExistsException
import com.slavacom.organizationservice.exception.EmployeeNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class EmployeesService(
	private val employeesRepository: EmployeesRepository,
	private val employeesMapper: EmployeesMapper,
	private val userServiceClient: UserServiceClient,
	private val authServiceClient: AuthServiceClient
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	fun list(orgId: UUID): List<EmployeeResponse> =
		employeesRepository.findAllByOrganizationIdAndIsActiveTrue(orgId)
			.map { employeesMapper.toResponse(it) }

	@Transactional
	fun add(orgId: UUID, request: AddEmployeeRequest): EmployeeResponse {
		if (employeesRepository.existsByUserIdAndOrganizationIdAndIsActiveTrue(request.userId, orgId)) {
			throw EmployeeAlreadyExistsException(request.userId, orgId)
		}
		val employee = employeesMapper.fromAddRequest(request)
		employee.organizationId = orgId

		// Create or get profile for the user
		val profileId = if (request.profileId != null) {
			logger.info("Using provided profileId: {}", request.profileId)
			request.profileId
		} else {
			logger.info("Creating new profile for userId={} in organizationId={}", request.userId, orgId)
			try {
				val profileResponse = userServiceClient.createProfile(request.userId, orgId)
				logger.info("Profile created: profileId={}", profileResponse.id)
				profileResponse.id
			} catch (e: Exception) {
				logger.error("Failed to create profile in UserService: {}", e.message)
				throw e
			}
		}

		employee.profileId = profileId
		val savedEmployee = employeesRepository.save(employee)
		logger.info("Employee saved: employeeId={}, userId={}, profileId={}, orgId={}", 
			savedEmployee.id, savedEmployee.userId, savedEmployee.profileId, orgId)

		// Update profile in AuthService for JWT generation
		return try {
			logger.info("Updating profile in AuthService: userId={}, profileId={}, organizationId={}", 
				request.userId, profileId, orgId)
			authServiceClient.updateProfile(request.userId, profileId, orgId)
			logger.info("Profile updated in AuthService for userId={}", request.userId)
			employeesMapper.toResponse(savedEmployee)
		} catch (e: AuthServiceException) {
			logger.warn("Failed to update profile in AuthService (non-blocking): {}", e.message)
			// Don't block employee addition if AuthService fails
			// User will get updated JWT on next login/refresh
			employeesMapper.toResponse(savedEmployee)
		} catch (e: Exception) {
			logger.error("Unexpected error updating profile in AuthService: {}", e.message)
			// Don't block employee addition
			employeesMapper.toResponse(savedEmployee)
		}
	}

    fun update(orgId: UUID, employeeId: UUID, request: UpdateEmployeeRequest): EmployeeResponse {
        val employee = employeesRepository.findByIdAndOrganizationId(employeeId, orgId)
            .orElseThrow { EmployeeNotFoundException("Employee $employeeId not found in organization $orgId") }
        request.role?.let { employee.role = it }
        request.permissions?.let { employee.permissions = it }
        return employeesMapper.toResponse(employeesRepository.save(employee))
    }

    fun remove(orgId: UUID, employeeId: UUID) {
        val employee = employeesRepository.findByIdAndOrganizationId(employeeId, orgId)
            .orElseThrow { EmployeeNotFoundException("Employee $employeeId not found in organization $orgId") }
        employee.isActive = false
        employeesRepository.save(employee)
    }

    @Transactional
    fun createOwner(userId: UUID, organizationId: UUID): EmployeeResponse {
        if (employeesRepository.existsByUserIdAndOrganizationIdAndIsActiveTrue(userId, organizationId)) {
            throw EmployeeAlreadyExistsException(userId, organizationId)
        }
        val employee = AddEmployeeRequest(
            userId = userId,
            role = EmployeeRole.OWNER
        )
        return add(organizationId, employee)
    }
}
