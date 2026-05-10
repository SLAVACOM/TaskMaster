package com.slavacom.organizationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingClientHttpRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long startTime = System.currentTimeMillis();

        String method = request.getMethod().name();
        String uri = request.getURI().toString();

        logger.debug("{} {} - request started", method, uri);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("{} {} completed in {}ms with status {}",
                    method,
                    uri,
                    duration,
                    response.getStatusCode().value());

            return response;
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("{} {} failed in {}ms with error: {}",
                    method,
                    uri,
                    duration,
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
