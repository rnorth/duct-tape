package org.rnorth.circuitbreakers;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author richardnorth
 */
public class AutoResetTests {

    private boolean invoked;

    @Before
    public void setUp() throws Exception {
        invoked = false;
    }

    @Test
    public void testRetryAfterExpiry() {
        TimeSource.DummyTimeSource dummyTimeSource = new TimeSource.DummyTimeSource();

        Breaker breaker = BreakerBuilder.newBuilder()
                .timeSource(dummyTimeSource)
                .autoResetAfter(5, TimeUnit.SECONDS)
                .build();

        // Simulate a failure
        assertEquals(State.ALIVE, breaker.getState());
        breaker.tryDo(() -> {
            throw new RuntimeException();
        });
        assertEquals(State.BROKEN, breaker.getState());

        // Next call should not fire the block
        breaker.tryDo(() -> fail("Should not be invoked"));
        assertEquals(State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(4999);
        breaker.tryDo(() -> fail("Should not be invoked"));
        assertEquals(State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(5000);
        breaker.tryDo(this::wasInvoked, () -> fail("Should not be invoked"));
        assertEquals(State.ALIVE, breaker.getState());
        assertTrue(invoked);
    }

    @Test
    public void testRetryGetAfterExpiry() {
        TimeSource.DummyTimeSource dummyTimeSource = new TimeSource.DummyTimeSource();

        Breaker breaker = BreakerBuilder.newBuilder()
                .timeSource(dummyTimeSource)
                .autoResetAfter(5, TimeUnit.SECONDS)
                .build();

        // Simulate a failure
        assertEquals(State.ALIVE, breaker.getState());
        breaker.tryGet(() -> {
            throw new RuntimeException();
        });
        assertEquals(State.BROKEN, breaker.getState());

        // Next call should not fire the block
        assertEquals("B", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals(State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(4999);
        assertEquals("B", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals(State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(5000);
        assertEquals("A", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals(State.ALIVE, breaker.getState());
    }

    private void wasInvoked() {
        invoked = true;
    }
}
