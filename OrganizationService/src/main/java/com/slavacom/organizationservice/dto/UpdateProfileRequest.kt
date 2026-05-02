package com.slavacom.organizationservice.dto

import java.util.UUID

data class UpdateProfileRequest(
    val userId: UUID,
    val profileId: UUID,
    val organizationId: UUID
)
