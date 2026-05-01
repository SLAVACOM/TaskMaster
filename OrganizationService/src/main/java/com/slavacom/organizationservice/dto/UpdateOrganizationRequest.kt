package com.slavacom.organizationservice.dto

data class UpdateOrganizationRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)
