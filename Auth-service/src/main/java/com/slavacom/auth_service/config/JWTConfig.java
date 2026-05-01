package com.slavacom.auth_service.config;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Slf4j
@Configuration
public class JWTConfig {

	@Value("${jwt.refresh.secret}")
	private String REFRESH_SECRET;

	@Value("${jwt.access.secret}")
	private String ACCESS_SECRET;

	@Bean
	public SecretKey jwtAccessSigningKey() {
		SecretKey key = Keys.hmacShaKeyFor(ACCESS_SECRET.getBytes(StandardCharsets.UTF_8));
		log.info("JWT access key fingerprint (first 4 bytes): {}",
				HexFormat.of().formatHex(key.getEncoded()).substring(0, 8));
		return key;
	}

	@Bean
	public SecretKey jwtRefreshSigningKey() {
		return Keys.hmacShaKeyFor(REFRESH_SECRET.getBytes(StandardCharsets.UTF_8));
	}

}
