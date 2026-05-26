package com.slavacom.userservice.config;

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

    @Bean("authServiceRestClient")
    public RestClient authServiceRestClient(@Value("${auth-service.url:http://localhost:8081}") String authServiceUrl) {
        return RestClient.builder().baseUrl(authServiceUrl).build();
    }
}
