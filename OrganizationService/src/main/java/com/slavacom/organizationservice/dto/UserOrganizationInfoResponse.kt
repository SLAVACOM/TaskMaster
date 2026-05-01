package com.slavacom.organizationservice.dto

data class UserOrganizationInfoResponse(
    val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val currentUserId: String,
    val role: String
)
