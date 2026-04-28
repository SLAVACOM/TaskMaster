package com.slavacom.s3storageservice.domain

data class UploadFileResponse(
    val fileName: String,
    val uploadUrl: String
)
