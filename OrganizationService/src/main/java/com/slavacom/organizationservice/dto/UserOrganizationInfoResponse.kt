package com.slavacom.organizationservice.dto

import com.slavacom.organizationservice.entity.EmployeeRole

data class UserOrganizationInfoResponse(
    val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val currentUserId: String,
    val role: EmployeeRole
)
