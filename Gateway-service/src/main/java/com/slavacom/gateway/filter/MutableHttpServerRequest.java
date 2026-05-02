package com.slavacom.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

/**
 * Wrapper that provides mutable headers for a ServerHttpRequest.
 * Used to work around Spring Cloud Gateway's read-only headers limitation.
 */
public class MutableHttpServerRequest extends ServerHttpRequestDecorator {

	private final HttpHeaders mutableHeaders;

	public MutableHttpServerRequest(ServerHttpRequest delegate, HttpHeaders mutableHeaders) {
		super(delegate);
		this.mutableHeaders = mutableHeaders;
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.mutableHeaders;
	}
}
