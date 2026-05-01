package com.slavacom.organizationservice.dto

data class CreateOrganizationRequest(
    val name: String,
    val description: String? = null
)
