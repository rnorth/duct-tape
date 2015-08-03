package org.rnorth.circuitbreakers.unreliables;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.fail;

/**
 * Created by rnorth on 23/07/2015.
 */
public class UnreliablesTest {

    @Test
    public void testRetryUntilTrueImmediateSuccess() throws Exception {
        try {
            Unreliables.retryUntilTrue(500, TimeUnit.MILLISECONDS, () -> true);
        } catch (TimeoutException e) {
            fail("When retrying until true, an immediate return true should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilTrueSuccessWithinTimeoutWindow() throws Exception {
        try {
            Unreliables.retryUntilTrue(500, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(300L);
                return true;
            });
        } catch (TimeoutException e) {
            fail("When retrying until true, a return true within the timeout window should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilTrueSuccessWithinTimeoutWindowWithManyFailures() throws Exception {
        long start = System.currentTimeMillis();
        try {
            Unreliables.retryUntilTrue(500, TimeUnit.MILLISECONDS, () -> {
                return System.currentTimeMillis() - start > 300;
            });
        } catch (TimeoutException e) {
            fail("When retrying until true, a return true within the timeout window should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilTrueFailsWhenOutsideTimeoutWindow() throws Exception {
        try {
            Unreliables.retryUntilTrue(500, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(600L);
                return true;
            });
            fail("When retrying until true, a return true outside the timeout window should throw a timeout exception");
        } catch (TimeoutException e) {
            // ok
        }
    }

    @Test
    public void testRetryUntilSuccessImmediateSuccess() throws Exception {
        try {
            String result = Unreliables.retryUntilSuccess(500, TimeUnit.MILLISECONDS, () -> "OK");
            assertEquals("A result can be returned using retryUntilSuccess", "OK", result);
        } catch (TimeoutException e) {
            fail("When retrying until true, an immediate return true should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilSuccessWithinTimeoutWindow() throws Exception {
        try {
            String result = Unreliables.retryUntilSuccess(500, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(300L);
                return "OK";
            });
            assertEquals("A result can be returned using retryUntilSuccess", "OK", result);
        } catch (TimeoutException e) {
            fail("When retrying until true, a return true within the timeout window should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilSuccessWithinTimeoutWindowWithManyFailures() throws Exception {
        long start = System.currentTimeMillis();
        try {
            String result = Unreliables.retryUntilSuccess(500, TimeUnit.MILLISECONDS, () -> {
                if (System.currentTimeMillis() - start < 300) {
                    throw new Exception("FAILURE");
                }
                return "OK";
            });
            assertEquals("A result can be returned using retryUntilSuccess", "OK", result);
        } catch (TimeoutException e) {
            fail("When retrying until true, a return true within the timeout window should be OK but timed out");
        }
    }

    @Test
    public void testRetryUntilSuccessFailsWhenOutsideTimeoutWindow() throws Exception {
        String result = "NOT OK";
        try {
            result = Unreliables.retryUntilSuccess(500, TimeUnit.MILLISECONDS, () -> {
                Thread.sleep(600L);
                return "OK";
            });
            fail("When retrying until true, a return true outside the timeout window should throw a timeout exception");
        } catch (TimeoutException e) {
            // ok
            assertEquals("A result can be returned using retryUntilSuccess", "NOT OK", result);
        }
    }

    @Test
    public void testRetryUntilSuccessFailsWhenOutsideTimeoutWindowAndCapturesException() throws Exception {
        try {
            Unreliables.retryUntilSuccess(500, TimeUnit.MILLISECONDS, () -> {
                throw new IllegalStateException("This is the exception");
            });
            fail("When retrying until true, a return true outside the timeout window should throw a timeout exception");
        } catch (TimeoutException e) {
            // ok
            assertEquals("A result can be returned using retryUntilSuccess", "This is the exception", e.getCause().getMessage());
        }
    }
}
