package com.scotiabank.studentsapi.config;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoggingWebFilterTest {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Mock
    private WebFilterChain filterChain;

    @InjectMocks
    private LoggingWebFilter loggingWebFilter;


    @Test
    void filter_GeneratesCorrelationId_WhenNotProvidedInRequest() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Assertions.assertNotNull(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER));
        Assertions.assertFalse(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER).isBlank());
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_GeneratesNewCorrelationId_WhenProvidedIsBlank() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students")
                .header(CORRELATION_ID_HEADER, "   ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Assertions.assertNotNull(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER));
        Assertions.assertFalse(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER).isBlank());
        Assertions.assertNotEquals("   ", exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER));
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_ReusesCorrelationId_WhenProvidedInRequest() {
        // Arrange
        String existingCorrelationId = "test-correlation-id-123";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students")
                .header(CORRELATION_ID_HEADER, existingCorrelationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Assertions.assertEquals(existingCorrelationId,
                exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER));
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_HandlesRequestWithQueryParams() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students?status=ACTIVE")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Assertions.assertNotNull(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER));
        Assertions.assertFalse(exchange.getResponse().getHeaders().getFirst(CORRELATION_ID_HEADER).isBlank());
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_PropagatesError_WhenChainFails() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students/1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new RuntimeException("Upstream error")));

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void filter_PropagatesError_WhenChainFailsWithStatusCodeAlreadySet() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/students/1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new RuntimeException("Upstream error")));

        // Act
        Mono<Void> result = loggingWebFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        verify(filterChain, times(1)).filter(exchange);
    }

}
