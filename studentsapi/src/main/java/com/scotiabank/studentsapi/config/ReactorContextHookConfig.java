package com.scotiabank.studentsapi.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

/**
 * Registers a global Reactor operator hook that lifts the Reactor
 * {@link reactor.util.context.Context} into SLF4J MDC for every operator in
 * every reactive pipeline.
 *
 * <p>This enables structured log fields such as {@code correlationId},
 * {@code requestId}, {@code httpMethod} and {@code httpPath} — written into the
 * context by {@link LoggingWebFilter} — to appear automatically in all log
 * entries produced anywhere downstream, regardless of which thread is running.
 */
@Slf4j
@Configuration
public class ReactorContextHookConfig {

    @PostConstruct
    public void enableMdcContextPropagation() {
        Hooks.onEachOperator(
                MdcContextLifter.MDC_CONTEXT_REACTOR_KEY,
                Operators.lift((scannable, subscriber) -> new MdcContextLifter<>(subscriber))
        );
        log.info("Reactor MDC context propagation hook registered");
    }

    @PreDestroy
    public void disableMdcContextPropagation() {
        Hooks.resetOnEachOperator(MdcContextLifter.MDC_CONTEXT_REACTOR_KEY);
        log.info("Reactor MDC context propagation hook removed");
    }
}
