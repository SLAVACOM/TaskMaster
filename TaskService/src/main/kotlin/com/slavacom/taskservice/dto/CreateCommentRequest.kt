package com.slavacom.taskservice.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateCommentRequest(
    @field:NotBlank
    val content: String,
    val parentCommentId: UUID? = null,
)
