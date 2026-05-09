package com.slavacom.notificationservice.config

import mu.KotlinLogging
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.io.IOException

private val logger = KotlinLogging.logger {}

@Component
class LoggingClientHttpRequestInterceptor : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val startTime = System.currentTimeMillis()
        val method = request.method.name
        val uri = request.uri.toString()

        logger.debug { "$method $uri - request started" }

        return try {
            val response = execution.execute(request, body)
            val duration = System.currentTimeMillis() - startTime

            logger.info { "$method $uri completed in $duration ms with status ${response.statusCode.value()}" }
            response
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            logger.error(e) { "$method $uri failed in $duration ms with error: ${e.message}" }
            throw e
        }
    }
}
