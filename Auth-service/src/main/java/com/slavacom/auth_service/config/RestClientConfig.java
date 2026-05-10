package com.slavacom.auth_service.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClientCustomizer restClientCustomizer(LoggingClientHttpRequestInterceptor loggingInterceptor) {
		return restClientBuilder -> restClientBuilder
				.requestInterceptor(loggingInterceptor);
	}
}

