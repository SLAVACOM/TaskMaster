package com.slavacom.organizationservice.dto

import java.util.UUID

data class ProfileResponse(
    val id: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val name: String
)
