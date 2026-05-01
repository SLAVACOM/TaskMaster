package com.slavacom.auth_service.service;

import com.slavacom.auth_service.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

	private final SecretKey jwtAccessSigningKey;
	private final SecretKey jwtRefreshSigningKey;

	@Value("${jwt.access.expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh.expiration}")
	private long refreshTokenExpiration;

	/**
	 * Генерация access токена
	 *
	 * @param userId ID пользователя
	 * @param role   роль пользователя
	 * @return access токен
	 */
	public String generateAccessToken(UUID userId, Role role) {
		Map<String, Object> claims = new HashMap<>();

		claims.put("role", role.name());
		claims.put("userId", userId.toString());
		return buildToken(claims, userId.toString(), accessTokenExpiration, jwtAccessSigningKey);
	}

	/**
	 * Генерация расширенного access токена с дополнительными данными
	 *
	 * @param userId         ID пользователя
	 * @param role           роль пользователя
	 * @param profileId      ID профиля пользователя
	 * @param organizationId ID организации пользователя
	 * @return access токен
	 */
	public String generateExtendedAccessToken(UUID userId, Role role, UUID profileId, UUID organizationId) {
		Map<String, Object> claims = new HashMap<>();

		claims.put("role", role.name());
		claims.put("userId", userId.toString());

		if (profileId != null) {
			claims.put("profileId", profileId.toString());
		}

		if (organizationId != null) {
			claims.put("organizationId", organizationId.toString());
		}

		return buildToken(claims, userId.toString(), accessTokenExpiration, jwtAccessSigningKey);
	}

	/**
	 * Генерация refresh токена
	 *
	 * @param userId ID пользователя
	 * @return refresh токен
	 */
	public String generateRefreshToken(UUID userId) {
		return buildToken(new HashMap<>(), userId.toString(), refreshTokenExpiration, jwtRefreshSigningKey);
	}

	private String buildToken(Map<String, Object> extraClaims, String subject, long expiration, SecretKey signingKey) {
		return Jwts.builder()
				.claims(extraClaims)
				.subject(subject)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(signingKey)
				.compact();
	}

	/**
	 * Извлечение userId из токена
	 */
	public UUID extractUserId(String token, boolean isRefreshToken) {
		String userIdStr = extractClaim(token, Claims::getSubject, isRefreshToken);
		return UUID.fromString(userIdStr);
	}

	/**
	 * Извлечение роли из access токена
	 */
	public Role extractRole(String accessToken) {
		Claims claims = extractAllClaims(accessToken, jwtAccessSigningKey);
		String roleStr = claims.get("role", String.class);
		return Role.valueOf(roleStr);
	}

	/**
	 * Извлечение profileId из access токена
	 */
	public UUID extractProfileId(String accessToken) {
		Claims claims = extractAllClaims(accessToken, jwtAccessSigningKey);
		String profileIdStr = claims.get("profileId", String.class);
		return profileIdStr != null ? UUID.fromString(profileIdStr) : null;
	}

	/**
	 * Извлечение organizationId из access токена
	 */
	public UUID extractOrganizationId(String accessToken) {
		Claims claims = extractAllClaims(accessToken, jwtAccessSigningKey);
		String organizationIdStr = claims.get("organizationId", String.class);
		return organizationIdStr != null ? UUID.fromString(organizationIdStr) : null;
	}

	/**
	 * Извлечение claim из токена
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefreshToken) {
		final Claims claims = extractAllClaims(token, isRefreshToken ? jwtRefreshSigningKey : jwtAccessSigningKey);
		return claimsResolver.apply(claims);
	}

	/**
	 * Проверка валидности токена
	 */
	public boolean isTokenValid(String token, boolean isRefreshToken) {
		try {
			extractAllClaims(token, isRefreshToken ? jwtRefreshSigningKey : jwtAccessSigningKey);
			return !isTokenExpired(token, isRefreshToken);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Проверка истечения срока действия токена
	 */
	private boolean isTokenExpired(String token, boolean isRefreshToken) {
		return extractExpiration(token, isRefreshToken).before(new Date());
	}

	/**
	 * Извлечение даты истечения токена
	 */
	private Date extractExpiration(String token, boolean isRefreshToken) {
		return extractClaim(token, Claims::getExpiration, isRefreshToken);
	}

	private Claims extractAllClaims(String token, SecretKey signingKey) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}

