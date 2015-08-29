package org.rnorth.ducttape;

import org.junit.Before;
import org.junit.Test;
import org.rnorth.ducttape.circuitbreakers.Breaker;
import org.rnorth.ducttape.circuitbreakers.BreakerBuilder;
import org.rnorth.ducttape.circuitbreakers.ExampleService;
import org.rnorth.ducttape.circuitbreakers.State;
import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.rnorth.ducttape.timeouts.Timeouts;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

/**
 * Created by rnorth on 29/08/2015.
 */
public class CompositeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeTest.class);
    private Breaker circuitBreaker;
    private RateLimiter rateLimiter;

    @Before
    public void setUp() throws Exception {
        circuitBreaker = BreakerBuilder.newBuilder().build();
        rateLimiter = RateLimiterBuilder.newBuilder()
                .withRate(20, TimeUnit.SECONDS)
                .withConstantThroughput()
                .build();
    }

    @Test
    public void simpleCompositeTest() throws Exception {
        ExampleService exampleService = new ExampleService(20L);
        String result = execute(exampleService);

        assertEquals("the result is passed through", "value", result);
        assertEquals("the breaker is not tripped", State.OK, circuitBreaker.getState());
        assertEquals("the service was only called once", 1, exampleService.getInvocationCount());
    }

    @Test
    public void simpleCompositeTestWhenTimingOut() throws Exception {
        ExampleService exampleService = new ExampleService(110L);
        String result = execute(exampleService);

        assertEquals("the default value is returned", "default value", result);
        assertEquals("the breaker is tripped", State.BROKEN, circuitBreaker.getState());
        int invocationCount = exampleService.getInvocationCount();
        assertTrue("the service was called less than 40 times (max 20 per second for 2 seconds)", invocationCount <= 40);
    }

    private String execute(ExampleService exampleService) throws Exception {

        return circuitBreaker.tryGet(() -> {
            return Unreliables.retryUntilSuccess(2, TimeUnit.SECONDS, () -> {
                return rateLimiter.getWhenReady(() -> {
                    return Timeouts.getWithTimeout(100, TimeUnit.MILLISECONDS, () -> {
                        return exampleService.getValue("Hello World");
                    });
                });
            });
        }, () -> {
            LOGGER.error("Circuit breaker was tripped");
        }, () -> {
            return "default value";
        });

    }
}
