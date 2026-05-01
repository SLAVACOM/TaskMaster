package com.slavacom.organizationservice.controller

data class UserOrganizationInfoResponse(
    val id: String,
    val name: String,
    val description: String?,
    val userId: String,
    val currentUserId: String,
    val role: String
)
