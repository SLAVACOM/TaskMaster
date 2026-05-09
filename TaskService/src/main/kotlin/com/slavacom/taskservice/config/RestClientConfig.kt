package com.slavacom.taskservice.config

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RestClientConfig {

    @Bean
    fun restClientCustomizer(loggingInterceptor: LoggingClientHttpRequestInterceptor): RestClientCustomizer {
        return RestClientCustomizer { restClientBuilder ->
            restClientBuilder.requestInterceptor(loggingInterceptor)
        }
    }
}
