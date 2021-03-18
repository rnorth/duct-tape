package org.rnorth.ducttape.unreliables;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.rnorth.ducttape.RetryCountExceededException;
import org.rnorth.ducttape.TimeoutException;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertThrows;
import static org.rnorth.visibleassertions.VisibleAssertions.fail;

/**
 * Tests for Unreliables class.
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
    public void testRetryUntilAnExceptionIsThrown() throws Exception {
        try {
            Unreliables.retryUntilTrue(5000, TimeUnit.MILLISECONDS, () -> {
                if (new Random().nextInt(2) == 1) {
                    throw new IllegalStateException();
                }
                return false;
            }, IllegalStateException.class);
        } catch (Exception e) {
            e = getRootCause(e);
            if (!e.getClass().equals(IllegalStateException.class)) {
                fail("When an exception is thrown execution should not be proceeded with");
            }
        }
    }

    @NotNull
    private Exception getRootCause(Exception e) {
        while (e.getCause() != null || !e.getClass().equals(IllegalStateException.class)) {
            e = (Exception) e.getCause();
        }
        return e;
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
    public void testRetryUntilSuccessLambdaStopsBeingCalledAfterTimeout() throws Exception {
        AtomicBoolean lambdaCalled = new AtomicBoolean();
        try {
            Unreliables.retryUntilSuccess(200, TimeUnit.MILLISECONDS, () -> {
                lambdaCalled.set(true);
                throw new RuntimeException();
            });
            fail("Expected TimeoutException on retryUntilSuccess that always fails.");
        } catch (TimeoutException e) {
            // ok
            Thread.sleep(200); // give worker thread time to stop calling the lambda
            lambdaCalled.set(false);
            Thread.sleep(200); // give worker thread time to call the lambda if it is still running
            assertFalse("Lambda should stop being executed when retryUntilSuccess times out.", lambdaCalled.get());
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

    @Test
    public void testRetryUntilSuccessPassesForSuccessWithinCount() throws Exception {

        final int[] attempt = {0};

        String result = Unreliables.retryUntilSuccess(3, () -> {
            attempt[0]++;
            if (attempt[0] == 2) {
                return "OK";
            } else {
                throw new IllegalStateException("This will fail sometimes");
            }
        });
        assertEquals("If success happens before the retry limit, that's OK", "OK", result);
    }

    @Test
    public void testRetryUntilSuccessFailsForFailuresOutsideCount() throws Exception {
        try {
            Unreliables.retryUntilSuccess(3, () -> {
                throw new IllegalStateException("This will always fail");
            });
            fail("When retrying until true, a return true outside the timeout window should throw a retry failure exception");
        } catch (RetryCountExceededException e) {
            // ok
            assertEquals("A result can be returned using retryUntilSuccess", "This will always fail", e.getCause().getMessage());
        }
    }

    @Test
    public void testRetryUntilTruePassesForSuccessWithinCount() throws Exception {

        final int[] attempt = {0};

        Unreliables.retryUntilTrue(3, () -> {
            attempt[0]++;
            if (attempt[0] == 2) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Test
    public void testRetryUntilTrueFailsForFailuresOutsideCount() throws Exception {

        assertThrows("When retrying until true, a return true outside the timeout window should throw a retry failure exception",
                RetryCountExceededException.class,
                () -> {
                    Unreliables.retryUntilTrue(3, () -> false);
        });
    }
}
