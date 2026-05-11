package com.slavacom.organizationservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
    @Value("\${services.user-service.url:http://localhost:8082}")
    private val userServiceUrl: String,

    @Value("\${services.auth-service.url:http://localhost:8081}")
    private val authServiceUrl: String,

    @Value("\${services.notification-service.url:http://localhost:8090}")
    private val notificationServiceUrl: String
) {

    @Bean("userServiceRestClient")
    fun userServiceRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(userServiceUrl)
            .build()
    }

    @Bean("authServiceRestClient")
    fun authServiceRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(authServiceUrl)
            .build()
    }

    @Bean("notificationServiceRestClient")
    fun notificationServiceRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(notificationServiceUrl)
            .build()
    }
}
