package com.slavacom.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Фильтр для добавления заголовков из exchange attributes в исходящий request
 * к микросервисам. Работает в паре с JwtAuthFilter.
 */
@Slf4j
@Component
public class RequestHeaderFilter implements GlobalFilter, Ordered {

	@Override
	public int getOrder() {
		return 0; // Выполняется после JwtAuthFilter (order = -1)
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		log.debug("RequestHeaderFilter: Processing request for path: {}", exchange.getRequest().getURI().getPath());
		log.debug("RequestHeaderFilter: Exchange attributes available: {}", exchange.getAttributes().keySet());

		// Получаем данные, сохраненные JwtAuthFilter в attributes
		String userId = (String) exchange.getAttributes().get("X-User-Id");
		String role = (String) exchange.getAttributes().get("X-User-Role");
		String profileId = (String) exchange.getAttributes().get("X-Profile-Id");
		String organizationId = (String) exchange.getAttributes().get("X-Organization-Id");

		log.debug("RequestHeaderFilter: Retrieved attributes - userId={}, role={}, profileId={}, organizationId={}",
				userId, role, profileId, organizationId);

		if (!StringUtils.hasText(userId)
				&& !StringUtils.hasText(role)
				&& !StringUtils.hasText(profileId)
				&& !StringUtils.hasText(organizationId)) {
			log.debug("RequestHeaderFilter: No JWT attributes found, skipping header injection");
			return chain.filter(exchange);
		}

		log.debug("RequestHeaderFilter: Original request headers: {}", exchange.getRequest().getHeaders().keySet());

		ServerHttpRequest newRequest = exchange.getRequest().mutate()
				.headers(headers -> {
					if (StringUtils.hasText(userId)) {
						headers.add("X-User-Id", userId);
						log.debug("RequestHeaderFilter: Added header X-User-Id={}", userId);
					}
					if (StringUtils.hasText(role)) {
						headers.add("X-User-Role", role);
						log.debug("RequestHeaderFilter: Added header X-User-Role={}", role);
					}
					if (StringUtils.hasText(profileId)) {
						headers.add("X-Profile-Id", profileId);
						log.debug("RequestHeaderFilter: Added header X-Profile-Id={}", profileId);
					}
					if (StringUtils.hasText(organizationId)) {
						headers.add("X-Organization-Id", organizationId);
						log.debug("RequestHeaderFilter: Added header X-Organization-Id={}", organizationId);
					}
				})
				.build();

		log.debug("RequestHeaderFilter: Mutated request headers: {}", newRequest.getHeaders().keySet());
		log.debug("RequestHeaderFilter: Forwarding request with mutated headers to chain");

		return chain.filter(exchange.mutate().request(newRequest).build());
	}
}
