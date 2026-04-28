package com.slavacom.taskservice.dto

import com.slavacom.taskservice.entity.FieldChange
import com.slavacom.taskservice.entity.enums.HistoryAction
import java.time.Instant
import java.util.UUID

data class TaskHistoryResponse(
    val id: UUID,
    val taskId: UUID,
    val changedBy: UUID,
    val changedAt: Instant?,
    val action: HistoryAction,
    val changes: List<FieldChange>,
)

