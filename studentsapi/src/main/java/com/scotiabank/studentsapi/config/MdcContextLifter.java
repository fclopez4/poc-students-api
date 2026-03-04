package com.scotiabank.studentsapi.config;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link CoreSubscriber} decorator that propagates selected keys from the
 * Reactor {@link Context} into SLF4J's {@link MDC} before each signal is
 * delivered downstream, and clears them afterwards.
 *
 * <p>Registered globally via {@link ReactorContextHookConfig} so that every
 * operator in the reactive pipeline benefits from MDC-enriched log entries.
 */
public class MdcContextLifter<T> implements CoreSubscriber<T> {

    static final String MDC_CONTEXT_REACTOR_KEY = MdcContextLifter.class.getName();

    private static final List<String> PROPAGATED_KEYS = List.of(
            "correlationId",
            "requestId",
            "httpMethod",
            "httpPath"
    );

    private final CoreSubscriber<T> delegate;

    MdcContextLifter(CoreSubscriber<T> delegate) {
        this.delegate = delegate;
    }

    // -------------------------------------------------------------------------
    // CoreSubscriber contract
    // -------------------------------------------------------------------------

    @Override
    public Context currentContext() {
        return delegate.currentContext();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        copyToMdc(delegate.currentContext());
        try {
            delegate.onSubscribe(subscription);
        } finally {
            clearMdc();
        }
    }

    @Override
    public void onNext(T value) {
        copyToMdc(delegate.currentContext());
        try {
            delegate.onNext(value);
        } finally {
            clearMdc();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        copyToMdc(delegate.currentContext());
        try {
            delegate.onError(throwable);
        } finally {
            clearMdc();
        }
    }

    @Override
    public void onComplete() {
        copyToMdc(delegate.currentContext());
        try {
            delegate.onComplete();
        } finally {
            clearMdc();
        }
    }

    // -------------------------------------------------------------------------
    // MDC helpers
    // -------------------------------------------------------------------------

    private void copyToMdc(Context context) {
        if (context.isEmpty()) {
            return;
        }
        Map<String, String> mdcEntries = PROPAGATED_KEYS.stream()
                .filter(context::hasKey)
                .collect(Collectors.toMap(key -> key, key -> context.get(key)));

        mdcEntries.forEach(MDC::put);
    }

    private void clearMdc() {
        PROPAGATED_KEYS.forEach(MDC::remove);
    }
}
