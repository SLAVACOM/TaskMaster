package com.slavacom.organizationservice.repository;

import com.slavacom.organizationservice.entity.Organization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface OrganizationRepository : JpaRepository<Organization, UUID>, JpaSpecificationExecutor<Organization> {
    fun findAllByIsActiveTrue(): List<Organization>
}

