package org.rnorth.circuitbreakers.inconsistents;

import org.junit.Test;
import org.rnorth.circuitbreakers.unreliables.TimeoutException;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;


/**
 * Created by rnorth on 23/07/2015.
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
}