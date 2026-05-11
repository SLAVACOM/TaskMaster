package com.slavacom.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient restClient() {
		return RestClient.builder().build();
	}

	@Bean("notificationServiceRestClient")
	public RestClient notificationServiceRestClient(
		@Value("${services.notification-service.url:http://localhost:8090}")
		String notificationServiceUrl
	) {
		return RestClient.builder()
			.baseUrl(notificationServiceUrl)
			.build();
	}
}

