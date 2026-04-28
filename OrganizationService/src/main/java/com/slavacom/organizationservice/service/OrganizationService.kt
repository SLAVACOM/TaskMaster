package com.slavacom.organizationservice.service

import com.slavacom.organizationservice.controller.OrganizationResponse
import com.slavacom.organizationservice.entity.Organization
import com.slavacom.organizationservice.exception.OrganizationNotFoundException
import com.slavacom.organizationservice.mapper.OrganizationMapper
import com.slavacom.organizationservice.repository.OrganizationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val organizationMapper: OrganizationMapper
) {

    fun getAll(isActive: Boolean = true): List<Organization> {
        val data = when (isActive) {
            true -> organizationRepository.findAllByIsActiveTrue()
            false -> organizationRepository.findAll()
        }

        return data
    }

    fun getById(id: UUID): OrganizationResponse {
        val organization = organizationRepository.findById(id)
            .orElseThrow() { OrganizationNotFoundException(id) }
        return organizationMapper.toOrganizationResponse(organization)
    }


}