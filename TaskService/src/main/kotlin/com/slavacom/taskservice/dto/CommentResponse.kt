package com.slavacom.taskservice.dto

import java.time.Instant
import java.util.UUID

data class CommentResponse(
    val id: UUID,
    val content: String,
    val createdBy: UUID,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val parentCommentId: UUID? = null,
    val replies: List<CommentResponse> = emptyList(),
    val attachments: List<AttachmentResponse> = emptyList(),
)
