package com.slavacom.organizationservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestClientConfig(
    @Value("\${services.user-service.url:http://localhost:8082}")
    private val userServiceUrl: String,

    @Value("\${services.auth-service.url:http://localhost:8081}")
    private val authServiceUrl: String
) {

    @Bean
    fun restClientCustomizer(loggingInterceptor: LoggingClientHttpRequestInterceptor): RestClientCustomizer {
        return RestClientCustomizer { restClientBuilder ->
            restClientBuilder.requestInterceptor(loggingInterceptor)
        }
    }
}
