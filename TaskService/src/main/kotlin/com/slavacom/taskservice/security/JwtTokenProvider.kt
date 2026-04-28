package com.slavacom.taskservice.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val jwtSecret: String,
) {
    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    fun extractClaims(token: String): Claims {
        val clean = if (token.startsWith("Bearer ")) token.substring(7) else token
        return try {
            val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(clean)
                .payload
        } catch (e: Exception) {
            log.error("JWT parse error: {}", e.message)
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token")
        }
    }

    fun extractUserId(token: String): UUID {
        val userId = extractClaims(token).get("userId", String::class.java)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "userId not found in token")
        return UUID.fromString(userId)
    }

    fun extractProfileId(token: String): UUID? {
        val v = extractClaims(token).get("profileId", String::class.java)
        return if (v.isNullOrBlank()) null else UUID.fromString(v)
    }

    fun extractOrganizationId(token: String): UUID? {
        val v = extractClaims(token).get("organizationId", String::class.java)
        return if (v.isNullOrBlank()) null else UUID.fromString(v)
    }

    fun extractRole(token: String): String? =
        extractClaims(token).get("role", String::class.java)
}

