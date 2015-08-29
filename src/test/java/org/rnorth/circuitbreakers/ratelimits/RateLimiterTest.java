package org.rnorth.circuitbreakers.ratelimits;

import org.junit.Test;
import org.rnorth.circuitbreakers.TimeoutException;
import org.rnorth.circuitbreakers.timeouts.Timeouts;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

/**
 * Tests for RateLimiter.
 */
public class RateLimiterTest {

    @Test
    public void testLimitExecutions() {

        int[] testWindow = new int[1];

        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                                             .withRate(10, TimeUnit.SECONDS)
                                             .withConstantThroughput()
                                             .build();

        try {
            Timeouts.doWithTimeout(2, TimeUnit.SECONDS, ()-> {
                //noinspection InfiniteLoopStatement
                while (true) {
                    rateLimiter.doWhenReady(() -> {
                        testWindow[0]++;
                    });
                }
            });
        } catch (TimeoutException ignored) {
            // We're just using a timeout here to limit execution to a given time
        }

        // Approximate estimates
        assertTrue("The rate limiter should have kept executions at or below 21", testWindow[0] <= 21);
        assertTrue("The rate limiter should allowed at least 15 executions", testWindow[0] >= 15);
    }

    @Test
    public void testLimitExecutionsAndGetResult() {

        int[] testWindow = new int[1];
        int[] lastValue = new int[1];

        RateLimiter rateLimiter = RateLimiterBuilder.newBuilder()
                                             .withRate(10, TimeUnit.SECONDS)
                                             .withConstantThroughput()
                                             .build();

        try {
            Timeouts.doWithTimeout(2, TimeUnit.SECONDS, ()-> {
                //noinspection InfiniteLoopStatement
                while (true) {
                    lastValue[0] = rateLimiter.doWhenReady(() -> {
                        return ++testWindow[0];
                    });
                }
            });
        } catch (TimeoutException ignored) {
            // We're just using a timeout here to limit execution to a given time
        }

        // Approximate estimates
        assertTrue("The rate limiter should have kept executions at or below 21", testWindow[0] <= 21);
        assertTrue("The rate limiter should allowed at least 15 executions", testWindow[0] >= 15);

        assertEquals("The rate limiter returns a result", testWindow[0], lastValue[0]);
    }
}