package com.slavacom.organizationservice.project.history

import java.time.Instant
import java.util.UUID

data class ProjectHistoryResponse(
    val id: UUID,
    val projectId: UUID,
    val changedBy: UUID?,
    val action: String,
    val changes: String?,
    val changedAt: Instant
)
