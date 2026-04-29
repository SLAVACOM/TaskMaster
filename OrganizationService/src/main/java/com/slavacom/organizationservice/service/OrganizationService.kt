package com.slavacom.organizationservice.service

import com.slavacom.organizationservice.controller.CreateOrganizationRequest
import com.slavacom.organizationservice.controller.OrganizationResponse
import com.slavacom.organizationservice.controller.UpdateOrganizationRequest
import com.slavacom.organizationservice.controller.UserOrganizationInfoResponse
import com.slavacom.organizationservice.employees.EmployeesRepository
import com.slavacom.organizationservice.exception.OrganizationNotFoundException
import com.slavacom.organizationservice.mapper.OrganizationMapper
import com.slavacom.organizationservice.repository.OrganizationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationMapper: OrganizationMapper,
    private val employeesRepository: EmployeesRepository
) {

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

    fun create(request: CreateOrganizationRequest, accountable: UUID): OrganizationResponse {
        val org = organizationMapper.fromCreateRequest(request)
        org.accountable = accountable
        return organizationMapper.toOrganizationResponse(organizationRepository.save(org))
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
