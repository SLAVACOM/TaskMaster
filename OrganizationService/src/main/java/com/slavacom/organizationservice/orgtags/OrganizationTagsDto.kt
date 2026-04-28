package com.slavacom.organizationservice.orgtags

import java.time.Instant
import java.util.UUID

data class OrgTagResponse(
    val id: UUID,
    val organizationId: UUID,
    val name: String,
    val color: String?,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateOrgTagRequest(
    val name: String,
    val color: String? = null,
    val description: String? = null
)

data class UpdateOrgTagRequest(
    val name: String? = null,
    val color: String? = null,
    val description: String? = null
)
