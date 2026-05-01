package com.slavacom.organizationservice.controller

data class CreateOrganizationRequest(
    val name: String,
    val description: String? = null
)
