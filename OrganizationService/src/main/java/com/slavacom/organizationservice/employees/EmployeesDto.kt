package com.slavacom.organizationservice.employees

import java.time.Instant
import java.util.UUID

data class EmployeeResponse(
    val id: UUID,
    val userId: UUID,
    val profileId: UUID?,
    val organizationId: UUID,
    val role: String,
    val permissions: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class AddEmployeeRequest(
    val userId: UUID,
    val profileId: UUID? = null,
    val role: String,
    val permissions: String? = null
)

data class UpdateEmployeeRequest(
    val role: String? = null,
    val permissions: String? = null
)
