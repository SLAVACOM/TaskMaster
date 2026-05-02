package com.slavacom.organizationservice.dto

import java.time.Instant
import java.util.*

data class OrganizationResponse(
    var id: UUID,
    var name: String,
    var description: String?,
    var accountable: UUID,
    var isActive: Boolean,
    var createdAt: Instant? = Instant.now(),
    var updatedAt: Instant?,
    var profileId: UUID? = null
)
