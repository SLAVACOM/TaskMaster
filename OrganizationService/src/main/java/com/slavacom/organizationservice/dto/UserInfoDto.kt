package com.slavacom.organizationservice.dto

import java.util.UUID

data class UserInfoDto(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
    val username: String
)
