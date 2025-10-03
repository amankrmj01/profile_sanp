package com.piandphi.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import java.util.function.Supplier;

public class ResilientScraper<T> {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilientScraper(String name) {
        this.circuitBreaker = CircuitBreaker.ofDefaults(name);
        this.retry = Retry.ofDefaults(name);
    }

    public T execute(Supplier<T> scraperCall, Supplier<T> fallback) {
        Supplier<T> decorated = Retry.decorateSupplier(retry,
                CircuitBreaker.decorateSupplier(circuitBreaker, scraperCall)
        );

        try {
            return decorated.get();
        } catch (Exception e) {
            System.err.println("ResilientScraper fallback triggered: " + e.getMessage());
            return fallback.get();
        }
    }
}
