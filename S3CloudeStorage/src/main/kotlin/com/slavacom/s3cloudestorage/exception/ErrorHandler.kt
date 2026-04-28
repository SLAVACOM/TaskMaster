package com.slavacom.s3storageservice.exception

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class ErrorHandler {

    private val log = LoggerFactory.getLogger(ErrorHandler::class.java)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParse(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val causeChain = generateSequence(ex.cause) { it.cause }
        val missingFieldCause = causeChain.find { it is tools.jackson.module.kotlin.KotlinInvalidNullException }

        val friendlyMessage = if (missingFieldCause != null) {
            val msg = missingFieldCause.message ?: ""
            val regex = "ArrayList\\[(\\d+)]".toRegex()
            val match = regex.find(msg)
            val position = match?.groups?.get(1)?.value?.toIntOrNull()

            if (msg.contains("fileName")) {
                if (position != null) {
                    "Missing required field 'fileName' at position $position"
                } else {
                    "Missing required field: fileName"
                }
            } else {
                "Missing required field in request"
            }
        } else {
            "Malformed JSON: ${ex.message?.substringBefore("\n") ?: "Invalid request body"}"
        }
        log.error("JSON parsing error: ${ex.message}", ex)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to friendlyMessage))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String?>> {
        log.warn("Validation failed", ex)
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(ex: ConstraintViolationException): Map<String, String?> {
        return ex.constraintViolations.associate { it.propertyPath.toString() to it.message }
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception): ResponseEntity<Map<String, String>> {
        log.error("Unexpected error", ex)
        return ResponseEntity(mapOf("error" to (ex.message ?: "Unknown error")), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}