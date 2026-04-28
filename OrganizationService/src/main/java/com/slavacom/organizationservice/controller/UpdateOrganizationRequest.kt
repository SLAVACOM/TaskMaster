package com.slavacom.organizationservice.controller

data class UpdateOrganizationRequest(
    val name: String? = null,
    val description: String? = null,
    val isActive: Boolean? = null
)
