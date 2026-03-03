package com.scotiabank.studentsapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Order(-1)
public class LoggingWebFilter implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant startTime = Instant.now();
        ServerHttpRequest request = exchange.getRequest();
        
        // Generate or extract correlation ID
        String correlationId = extractOrGenerateCorrelationId(request);
        String requestId = UUID.randomUUID().toString();
        
        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        
        // Extract request information
        String httpMethod = request.getMethod().toString();
        String httpPath = request.getPath().value();
        String queryParams = request.getURI().getQuery();
        String fullPath = queryParams != null ? httpPath + "?" + queryParams : httpPath;
        
        log.debug("Incoming request: {} {} [correlationId={}, requestId={}]", 
                httpMethod, fullPath, correlationId, requestId);
        
        // Continue the filter chain with context
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx
                        .put(CORRELATION_ID_KEY, correlationId)
                        .put(REQUEST_ID_KEY, requestId)
                        .put("httpMethod", httpMethod)
                        .put("httpPath", httpPath))
                .doOnSuccess(v -> logResponse(exchange, httpMethod, fullPath, correlationId, 
                        requestId, startTime))
                .doOnError(error -> logError(exchange, httpMethod, fullPath, correlationId, 
                        requestId, startTime, error))
                .contextWrite(Context.of(
                        CORRELATION_ID_KEY, correlationId,
                        REQUEST_ID_KEY, requestId,
                        "httpMethod", httpMethod,
                        "httpPath", httpPath
                ));
    }
    
    private String extractOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }
    
    private void logResponse(ServerWebExchange exchange, String httpMethod, String httpPath, 
                            String correlationId, String requestId, Instant startTime) {
        int statusCode = exchange.getResponse().getStatusCode() != null 
                ? exchange.getResponse().getStatusCode().value() 
                : 0;
        
        long duration = Duration.between(startTime, Instant.now()).toMillis();
        
        log.info("Request completed: {} {} - Status: {} - Duration: {}ms [correlationId={}, requestId={}]",
                httpMethod, httpPath, statusCode, duration, correlationId, requestId);
    }
    
    private void logError(ServerWebExchange exchange, String httpMethod, String httpPath, 
                         String correlationId, String requestId, Instant startTime, Throwable error) {
        int statusCode = exchange.getResponse().getStatusCode() != null 
                ? exchange.getResponse().getStatusCode().value() 
                : 500;
        
        long duration = Duration.between(startTime, Instant.now()).toMillis();
        
        log.error("Request failed: {} {} - Status: {} - Duration: {}ms [correlationId={}, requestId={}] - Error: {}",
                httpMethod, httpPath, statusCode, duration, correlationId, requestId, 
                error.getMessage(), error);
    }
}
