package com.slavacom.taskservice.dto

import java.time.Instant
import java.util.UUID

data class AttachmentResponse(
    val id: UUID,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val createdAt: Instant,
)
