package com.slavacom.organizationservice.exception

import org.springframework.http.HttpStatus

class AuthServiceException(
    message: String,
    val statusCode: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
