package org.rnorth.ducttape.inconsistents;

import org.junit.Test;
import org.rnorth.ducttape.TimeoutException;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;


/**
 * Tests for Inconsistents class.
 */
public class InconsistentsTest {

    @Test
    public void testEventualConsistent() {
        long start = System.currentTimeMillis();
        Long result = Inconsistents.retryUntilConsistent(50, 1000, TimeUnit.MILLISECONDS, () -> {
            Thread.sleep(10L);
            // this result won't be consistent until after 100ms
            long now = System.currentTimeMillis();
            return (now - start) / 100;
        });
    }

    @Test
    public void testNeverConsistent() {
        try {
            Inconsistents.retryUntilConsistent(50, 1000, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(10L);
                return System.currentTimeMillis();
            });
        } catch (TimeoutException e) {
            Throwable cause = e.getCause();
            assertEquals("An exception is thrown if the result is never consistent", ResultsNeverConsistentException.class, cause.getClass());
        }
    }

    @Test
    public void testNotConsistentLongEnough() {
        try {
            Inconsistents.retryUntilConsistent(50, 1000, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(10L);
                return System.currentTimeMillis() / 49;
            });
        } catch (TimeoutException e) {
            Throwable cause = e.getCause();
            assertEquals("An exception is thrown if the result is never consistent", InconsistentResultsException.class, cause.getClass());
        }
    }

    @Test
    public void testUnitConversion() {
        long start = System.currentTimeMillis();

        Inconsistents.retryUntilConsistent(1, 5, TimeUnit.SECONDS, () -> {
            Thread.sleep(10L);
            // this result won't be consistent until after 1.5s
            long now = System.currentTimeMillis();
            return (now - start) / 1500;
        });

        assertTrue("At least one second elapsed", System.currentTimeMillis() - start > 1000);
    }
}