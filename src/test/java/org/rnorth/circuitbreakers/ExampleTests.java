package org.rnorth.circuitbreakers;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Examples for inclusion in documentation.
 *
 * @author richardnorth
 */
public class ExampleTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleTests.class);

    @Test
    public void exampleUsage() {

        Breaker breaker = BreakerBuilder.newBuilder().build();
        ExampleService someUnreliableService = new ExampleService();
        Queue<Object> fallbackQueue = new PriorityQueue<>();
        Object message = new Object();
        String key = "key";
        Map<String, String> fallbackCache = new HashMap<>();

        // tag::ExampleDoFailFallback[]
        breaker.tryDo(() -> {
            someUnreliableService.sendMessage(message);
        }, () -> {
            LOGGER.error("Service failed!");
        }, () -> {
            fallbackQueue.add(message);
        });
        // end::ExampleDoFailFallback[]

        // tag::ExampleDoFallback[]
        breaker.tryDo(() -> {
            someUnreliableService.sendMessage(message);
        }, () -> {
            fallbackQueue.add(message);
        });
        // end::ExampleDoFallback[]

        // tag::ExampleDo[]
        breaker.tryDo(() -> {
            someUnreliableService.sendMessage(message);
        });
        // end::ExampleDo[]

        // tag::ExampleGetFailFallback[]
        String response = breaker.tryGet(() -> {
            return someUnreliableService.getValue(key);
        }, () -> {
            LOGGER.error("Service failed!");
        }, () -> {
            return fallbackCache.get(key);
        });
        // end::ExampleGetFailFallback[]

        // tag::ExampleGetFallback[]
        response = breaker.tryGet(() -> {
            return someUnreliableService.getValue(key);
        }, () -> {
            return fallbackCache.get(key);
        });
        // end::ExampleGetFallback[]

        // tag::ExampleGet[]
        Optional<String> optional = breaker.tryGet(() -> {
            return someUnreliableService.getValue(key);
        });

        if (optional.isPresent()) {
            // do something with optional.get()
        }
        // end::ExampleGet[]
    }

    @Test
    public void exampleBuilder() {

        ConcurrentMap<String, Object> myMap = new ConcurrentHashMap<>();
        Breaker breaker;

        // tag::ExampleSimpleBuild[]
        breaker = BreakerBuilder.newBuilder()
                        .build();
        // end::ExampleSimpleBuild[]

        // tag::ExampleAutoResetBuild[]
        breaker = BreakerBuilder.newBuilder()
                        .autoResetAfter(1, TimeUnit.MINUTES)
                        .build();
        // end::ExampleAutoResetBuild[]

        // tag::ExampleExternalStoreBuild[]
        breaker = BreakerBuilder.newBuilder()
                        .storeStateIn(myMap, "ExampleCircuitBreaker")
                        .build();
        // end::ExampleExternalStoreBuild[]

    }
}
