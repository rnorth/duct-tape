package org.rnorth.circuitbreakers.unreliables;

import org.rnorth.circuitbreakers.timeouts.Timeouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Utilities to support automatic retry of things that may fail.
 */
public abstract class Unreliables {

    private static final Logger LOGGER = LoggerFactory.getLogger(Unreliables.class);

    /**
     * Call a supplier repeatedly until it returns a result. If an exception is thrown, the call
     * will be retried repeatedly until the timeout is hit.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     * @param <T>      return type of the supplier
     * @return the result of the successful lambda expression call
     */
    public static <T> T retryUntilSuccess(final int timeout, final TimeUnit timeUnit, final Callable<T> lambda) {
        final int[] attempt = {0};
        final Exception[] lastException = {null};

        try {
            return Timeouts.withTimeout(timeout, timeUnit, () -> {
                while (true) {
                    try {
                        return lambda.call();
                    } catch (Exception e) {
                        // Failed
                        LOGGER.trace("Retrying lambda call on attempt {}", attempt[0]++);
                        lastException[0] = e;
                    }
                }
            });
        } catch (org.rnorth.circuitbreakers.TimeoutException e) {
            if (lastException[0] != null) {
                throw new org.rnorth.circuitbreakers.TimeoutException("Timeout waiting for result with exception", lastException[0]);
            } else {
                throw new org.rnorth.circuitbreakers.TimeoutException(e);
            }
        }
    }

    /**
     * Call a callable repeatedly until it returns true. If an exception is thrown, the call
     * will be retried repeatedly until the timeout is hit.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression
     */
    public static void retryUntilTrue(final int timeout, final TimeUnit timeUnit, final Callable<Boolean> lambda) {
        retryUntilSuccess(timeout, timeUnit, () -> {
            if (!lambda.call()) {
                throw new RuntimeException("Not ready yet");
            } else {
                return null;
            }
        });
    }
}
