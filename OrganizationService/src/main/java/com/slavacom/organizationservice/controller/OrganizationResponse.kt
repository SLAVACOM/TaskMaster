package com.slavacom.organizationservice.controller

import java.time.Instant
import java.util.*

/**
 * DTO for [com.slavacom.organizationservice.entity.Organization]
 */
data class OrganizationResponse(
    var id: UUID,
    var name: String,
    var description: String,
    var accountable: UUID,
    var createdAt: Instant,
    var updatedAt: Instant
)