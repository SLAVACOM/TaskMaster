package com.slavacom.s3cloudestorage.domain

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class DownloadFileRequest(
    @field:NotEmpty(message = "Filename must not be empty")
    val fileName: String
)

data class DownloadFilesRequest(
    @field:NotEmpty(message = "Files list cannot be empty")
    val files: List<@Valid DownloadFileRequest> = emptyList()
)