package com.slavacom.organizationservice.employees

import com.slavacom.organizationservice.entity.EmployeeRole
import java.time.Instant
import java.util.UUID

data class EmployeeResponse(
    val id: UUID,
    val userId: UUID,
    val profileId: UUID?,
    val organizationId: UUID,
    val role: EmployeeRole,
    val permissions: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class AddEmployeeRequest(
    val userId: UUID,
    val profileId: UUID? = null,
    val role: EmployeeRole,
    val permissions: String? = null
)

data class UpdateEmployeeRequest(
    val role: EmployeeRole? = null,
    val permissions: String? = null
)
