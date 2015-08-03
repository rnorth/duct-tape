package org.rnorth.circuitbreakers.circuitbreakers;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.rnorth.visibleassertions.VisibleAssertions.fail;

/**
 * @author richardnorth
 */
public class AutoResetTest {

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
        assertEquals("The breaker state is initially OK", State.OK, breaker.getState());
        breaker.tryDo(() -> {
            throw new RuntimeException();
        });
        assertEquals("The breaker trips after a failure", State.BROKEN, breaker.getState());

        // Next call should not fire the block
        breaker.tryDo(() -> fail("Should not be invoked"));
        assertEquals("The breaker remains broken when called again", State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(4999);
        breaker.tryDo(() -> fail("Should not be invoked"));
        assertEquals("Just before the breaker is due to reset, it remains broken", State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(5000);
        breaker.tryDo(this::wasInvoked, () -> fail("Should not be invoked"));
        assertEquals("After the reset time has elapsed, the breaker resets to OK", State.OK, breaker.getState());
        assertTrue("After the reset time has elapsed, callables are invoked again", invoked);
    }

    @Test
    public void testRetryGetAfterExpiry() {
        TimeSource.DummyTimeSource dummyTimeSource = new TimeSource.DummyTimeSource();

        Breaker breaker = BreakerBuilder.newBuilder()
                .timeSource(dummyTimeSource)
                .autoResetAfter(5, TimeUnit.SECONDS)
                .build();

        // Simulate a failure
        assertEquals("The breaker state is initially OK", State.OK, breaker.getState());
        breaker.tryGet(() -> {
            throw new RuntimeException();
        });
        assertEquals("The breaker trips after a failure", State.BROKEN, breaker.getState());

        // Next call should not fire the block
        assertEquals("The fallback supplier is invoked after the breaker is tripped", "B", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals("The breaker remains broken when called again", State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(4999);
        assertEquals("Just before the breaker is due to reset, it remains broken and the fallback supplier is invoked", "B", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals("Just before the breaker is due to reset, it remains broken", State.BROKEN, breaker.getState());

        dummyTimeSource.setCurrentTimeMillis(5000);
        assertEquals("After the reset time has elapsed, suppliers are invoked again", "A", breaker.tryGet(() -> "A", () -> "B"));
        assertEquals("After the reset time has elapsed, the breaker resets to OK", State.OK, breaker.getState());
    }

    private void wasInvoked() {
        invoked = true;
    }
}
