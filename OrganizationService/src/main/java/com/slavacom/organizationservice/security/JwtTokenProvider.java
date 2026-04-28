package com.slavacom.organizationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key}")
    private String jwtSecret;

    /**
     * Извлечение claims из JWT токена
     */
    public Claims extractClaims(String token) {
        try {
            // Убираем "Bearer " если есть
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        }
    }

    /**
     * Получение ID пользователя из JWT токена
     * ВНИМАНИЕ: Этот метод больше не работает т.к. Spring Security отключен
     * Используйте extractUserIdFromToken(String token) вместо этого
     */
    public UUID getCurrentUserId() {
        throw new UnsupportedOperationException("Spring Security is disabled. Use extractUserIdFromToken(String token) method instead. Pass JWT token from Authorization header.");
    }

    /**
     * Получение ID пользователя из JWT токена
     */
    public UUID extractUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        String userIdStr = claims.get("userId", String.class);

        if (userIdStr == null) {
            throw new RuntimeException("UserId not found in JWT token");
        }

        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid userId format in JWT token: " + userIdStr);
        }
    }

    /**
     * Получение роли пользователя из JWT токена
     */
    public String getCurrentUserRole() {
        throw new RuntimeException("JWT token support is disabled. Use extractRoleFromToken(String token) method instead.");
    }

    /**
     * Получение роли пользователя из JWT токена
     */
    public String extractRoleFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Получение ID организации из JWT токена (если есть)
     */
    public UUID getCurrentUserOrganizationId() {
        throw new RuntimeException("JWT token support is disabled. Use extractOrganizationIdFromToken(String token) method instead.");
    }

    /**
     * Получение ID организации из JWT токена
     */
    public UUID extractOrganizationIdFromToken(String token) {
        Claims claims = extractClaims(token);
        String organizationIdStr = claims.get("organizationId", String.class);

        if (organizationIdStr == null || organizationIdStr.isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(organizationIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid organizationId format in JWT token: {}", organizationIdStr);
            return null;
        }
    }

    /**
     * Получение ID профиля из JWT токена (если есть)
     */
    public UUID getCurrentUserProfileId() {
        throw new RuntimeException("JWT token support is disabled. Use extractProfileIdFromToken(String token) method instead.");
    }

    /**
     * Получение ID профиля из JWT токена
     */
    public UUID extractProfileIdFromToken(String token) {
        Claims claims = extractClaims(token);
        String profileIdStr = claims.get("profileId", String.class);

        if (profileIdStr == null || profileIdStr.isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(profileIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid profileId format in JWT token: {}", profileIdStr);
            return null;
        }
    }
}
