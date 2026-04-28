package com.slavacom.s3storageservice.domain

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class UploadFileRequest(
    val category: String = "uploads",
    val userId: String = "unauthorized",

    @field:NotEmpty(message = "Filename must not be empty")
    val originalFilename: String
)


data class UploadFilesRequest(
    @field:NotEmpty(message = "Files list cannot be empty")
    val files: List<@Valid UploadFileRequest> = emptyList()
)
