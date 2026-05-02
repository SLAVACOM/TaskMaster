package com.slavacom.organizationservice.exception

import org.springframework.http.HttpStatus

class UserServiceException(
    message: String,
    val statusCode: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)
