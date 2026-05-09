package com.slavacom.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long startTime = System.currentTimeMillis();

        String method = request.getMethod().name();
        String uri = request.getURI().toString();

        log.debug("{} {} - request started", method, uri);

        try {
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - startTime;

            log.info("{} {} completed in {}ms with status {}",
                    method,
                    uri,
                    duration,
                    response.getStatusCode().value());

            return response;
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{} {} failed in {}ms with error: {}",
                    method,
                    uri,
                    duration,
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}
