package com.slavacom.authservice.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Configuration
public class JWTConfig {

	@Value("${jwt.refresh.secret}")
	private String REFRESH_SECRET;

	@Value("${jwt.access.secret}")
	private String ACCESS_SECRET;

	@Bean
	public Key jwtAccessSigningKey() {
		return Keys.hmacShaKeyFor(ACCESS_SECRET.getBytes(StandardCharsets.UTF_8));
	}

	@Bean
	public Key jwtRefreshSigningKey() {
		return Keys.hmacShaKeyFor(REFRESH_SECRET.getBytes(StandardCharsets.UTF_8));
	}

}
