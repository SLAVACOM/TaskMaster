package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.entity.Employees
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface EmployeesRepository : JpaRepository<Employees, UUID> {
    fun findAllByOrganizationIdAndIsActiveTrue(organizationId: UUID): List<Employees>
    fun findByIdAndOrganizationId(id: UUID, organizationId: UUID): Optional<Employees>
    fun existsByUserIdAndOrganizationIdAndIsActiveTrue(userId: UUID, organizationId: UUID): Boolean
    fun findByUserIdAndIsActiveTrue(userId: UUID): Optional<Employees>
    fun findByUserIdAndOrganizationIdAndIsActiveTrue(userId: UUID, organizationId: UUID): Optional<Employees>
}
