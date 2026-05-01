package com.slavacom.s3cloudestorage.domain

data class UploadFileResponse(
    val fileName: String,
    val uploadUrl: String
)
