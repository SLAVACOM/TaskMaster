package com.slavacom.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
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
		// Получаем данные, сохраненные JwtAuthFilter в attributes
		String userId = (String) exchange.getAttributes().getOrDefault("X-User-Id", "");
		String role = (String) exchange.getAttributes().getOrDefault("X-User-Role", "");
		String profileId = (String) exchange.getAttributes().getOrDefault("X-Profile-Id", "");
		String organizationId = (String) exchange.getAttributes().getOrDefault("X-Organization-Id", "");

		// Добавляем заголовки в request если есть данные
		if (!userId.isEmpty() || !role.isEmpty() || !profileId.isEmpty() || !organizationId.isEmpty()) {
			try {
				ServerHttpRequest newRequest = exchange.getRequest().mutate()
						.header("X-User-Id", userId)
						.header("X-User-Role", role)
						.header("X-Profile-Id", profileId)
						.header("X-Organization-Id", organizationId)
						.build();

				return chain.filter(exchange.mutate().request(newRequest).build());
			} catch (UnsupportedOperationException e) {
				log.debug("Cannot modify request headers (headers are immutable), continuing without headers");
				return chain.filter(exchange);
			}
		}

		return chain.filter(exchange);
	}
}

