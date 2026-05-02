package com.slavacom.organizationservice.dto

import java.util.UUID

data class CreateProfileRequest(
    val userId: UUID,
    val organizationId: UUID,
    val name: String = "Default Profile"
)
