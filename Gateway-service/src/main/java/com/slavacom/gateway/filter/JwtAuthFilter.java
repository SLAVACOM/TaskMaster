package com.slavacom.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

	private static final List<String> PUBLIC_PATHS = List.of(
			"/api/auth/"
	);

	@Value("${jwt.access.secret}")
	private String ACCESS_SECRET;

	@PostConstruct
	public void logKeyFingerprint() {
		SecretKey key = Keys.hmacShaKeyFor(ACCESS_SECRET.getBytes(StandardCharsets.UTF_8));
		log.info("Gateway JWT access key fingerprint (first 4 bytes): {}",
				HexFormat.of().formatHex(key.getEncoded()).substring(0, 8));
	}

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());

		String path = exchange.getRequest().getURI().getPath();

		if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
			log.info("Public path accessed: {}", path);
			return chain.filter(exchange);
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (authHeader == null) {

			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			log.warn("Missing or invalid Authorization header for path {}: {}", path, authHeader);
			return exchange.getResponse().setComplete();
		}

		if (authHeader.startsWith("Bearer ")) {
			authHeader = authHeader.substring(7);
		}

		final String token = authHeader;
		return Mono.fromCallable(() -> parseClaims(token))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(claims -> {

					String userId = firstNonBlank(claims.get("userId", String.class), claims.getSubject());
					String role = claims.get("role", String.class);
					String profileId = claims.get("profileId", String.class);
					String organizationId = claims.get("organizationId", String.class);

					putAttributeIfHasText(exchange, "X-User-Id", userId);
					putAttributeIfHasText(exchange, "X-User-Role", role);
					putAttributeIfHasText(exchange, "X-Profile-Id", profileId);
					putAttributeIfHasText(exchange, "X-Organization-Id", organizationId);

					log.info("JWT validated: path={} userId={} role={}", path, userId, role);
					log.debug("JWT attributes stored in exchange: X-User-Id={}, X-User-Role={}, X-Profile-Id={}, X-Organization-Id={}",
							userId, role, profileId, organizationId);

					return chain.filter(exchange);
				})
				.onErrorResume(e -> {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					log.warn("JWT validation failed for path {}: {}", path, e.getMessage());
					log.error("Error", e);
					return exchange.getResponse().setComplete();
				});
	}

	private void putAttributeIfHasText(ServerWebExchange exchange, String key, String value) {
		if (StringUtils.hasText(value)) {
			exchange.getAttributes().put(key, value);
		}
	}

	private String firstNonBlank(String primary, String fallback) {
		return StringUtils.hasText(primary) ? primary : fallback;
	}

	private Claims parseClaims(String token) {
		if (token == null || token.isEmpty()) {
			throw new IllegalArgumentException("Token is empty");
		}

		if (ACCESS_SECRET == null || ACCESS_SECRET.isEmpty()) {
			log.error("JWT_SECRET is not configured!");
			throw new IllegalStateException("JWT_SECRET not configured");
		}

		try {
			SecretKey key = Keys.hmacShaKeyFor(ACCESS_SECRET.getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token.trim())
					.getPayload();

			log.debug("JWT parsed successfully. Claims: {}", claims);
			return claims;
		} catch (Exception e) {
			log.error("Failed to parse JWT token: {}; SECRET length: {}; Token preview: {}",
					e.getMessage(),
					ACCESS_SECRET != null ? ACCESS_SECRET.length() : 0,
					token.length() > 20 ? token.substring(0, 20) + "..." : token,
					e);
			throw new IllegalArgumentException("Invalid JWT token", e);
		}
	}
}
