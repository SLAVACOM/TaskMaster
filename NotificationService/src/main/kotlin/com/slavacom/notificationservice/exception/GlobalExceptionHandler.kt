package com.slavacom.notificationservice.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
private class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidRequest(ex: HttpMessageNotReadableException): ResponseEntity<String> {
        // Log the exception
        println("Invalid request: ${ex.message}")

        return ResponseEntity(
            "Invalid request format. Please check your input and try again.",
            HttpStatus.BAD_REQUEST
        )
    }


    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, String>> {
        // Log the exception (you can use a logging framework like Log4j or SLF4J)
        println("An error occurred: ${ex.message}")
        return ResponseEntity(
            mapOf("error" to (ex.message ?: "Unknown error")),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EmailIsRequiredException::class)
    @ResponseBody
    fun handleEmailIsRequiredException(ex: EmailIsRequiredException): ResponseEntity<Map<String,
            String>> {
        return ResponseEntity(
            mapOf("error" to ex.message!!),
            HttpStatus.BAD_REQUEST
        )
    }
}

