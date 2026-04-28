package com.slavacom.auth_service.service;

import com.slavacom.auth_service.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private Key jwtAccessSigningKey;

	@Autowired
	private Key jwtRefreshSigningKey;

	private UUID testUserId;
	private Role testRole;

	@BeforeEach
	void setUp() {
		testUserId = UUID.randomUUID();
		testRole = Role.USER;
	}

	@Test
	void generateAccessToken_Success() {
		// Act
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Assert
		assertNotNull(token);
		assertFalse(token.isEmpty());
	}

	@Test
	void generateRefreshToken_Success() {
		// Act
		String token = jwtService.generateRefreshToken(testUserId);

		// Assert
		assertNotNull(token);
		assertFalse(token.isEmpty());
	}

	@Test
	void extractUserId_FromAccessToken_Success() {
		// Arrange
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Act
		UUID extractedUserId = jwtService.extractUserId(token, false);

		// Assert
		assertEquals(testUserId, extractedUserId);
	}

	@Test
	void extractUserId_FromRefreshToken_Success() {
		// Arrange
		String token = jwtService.generateRefreshToken(testUserId);

		// Act
		UUID extractedUserId = jwtService.extractUserId(token, true);

		// Assert
		assertEquals(testUserId, extractedUserId);
	}

	@Test
	void extractRole_FromAccessToken_Success() {
		// Arrange
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Act
		Role extractedRole = jwtService.extractRole(token);

		// Assert
		assertEquals(testRole, extractedRole);
	}

	@Test
	void isTokenValid_ValidAccessToken_ReturnsTrue() {
		// Arrange
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Act
		boolean isValid = jwtService.isTokenValid(token, false);

		// Assert
		assertTrue(isValid);
	}

	@Test
	void isTokenValid_ValidRefreshToken_ReturnsTrue() {
		// Arrange
		String token = jwtService.generateRefreshToken(testUserId);

		// Act
		boolean isValid = jwtService.isTokenValid(token, true);

		// Assert
		assertTrue(isValid);
	}

	@Test
	void isTokenValid_InvalidToken_ReturnsFalse() {
		// Arrange
		String invalidToken = "invalid.token.here";

		// Act
		boolean isValid = jwtService.isTokenValid(invalidToken, false);

		// Assert
		assertFalse(isValid);
	}

	@Test
	void isTokenValid_AccessTokenValidatedAsRefresh_ReturnsFalse() {
		// Arrange
		String accessToken = jwtService.generateAccessToken(testUserId, testRole);

		// Act - пытаемся валидировать access токен как refresh
		boolean isValid = jwtService.isTokenValid(accessToken, true);

		// Assert
		assertFalse(isValid);
	}

	@Test
	void accessTokenContainsRoleClaim() {
		// Arrange
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Act
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(jwtAccessSigningKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

		// Assert
		assertNotNull(claims.get("role"));
		assertEquals(testRole.name(), claims.get("role", String.class));
	}

	@Test
	void accessTokenContainsUserIdClaim() {
		// Arrange
		String token = jwtService.generateAccessToken(testUserId, testRole);

		// Act
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(jwtAccessSigningKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

		// Assert
		assertNotNull(claims.get("userId"));
		assertEquals(testUserId.toString(), claims.get("userId", String.class));
	}

	@Test
	void refreshTokenContainsSubject() {
		// Arrange
		String token = jwtService.generateRefreshToken(testUserId);

		// Act
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(jwtRefreshSigningKey)
				.build()
				.parseClaimsJws(token)
				.getBody();

		// Assert
		assertNotNull(claims.getSubject());
		assertEquals(testUserId.toString(), claims.getSubject());
	}
}

