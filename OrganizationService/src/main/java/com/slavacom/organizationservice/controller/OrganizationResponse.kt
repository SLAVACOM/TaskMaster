package com.slavacom.organizationservice.controller

import java.time.Instant
import java.util.*

data class OrganizationResponse(
    var id: UUID,
    var name: String,
    var description: String?,
    var accountable: UUID,
    var isActive: Boolean,
    var createdAt: Instant,
    var updatedAt: Instant?
)
