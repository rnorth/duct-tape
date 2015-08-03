package org.rnorth.circuitbreakers.unreliables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Utilities to support automatic retry of things that may tend to not always produce consistent results.
 */
public abstract class Unreliables {

    private static final Logger LOGGER = LoggerFactory.getLogger(Unreliables.class);

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    /**
     * Call a supplier repeatedly until it returns a result. If an exception is thrown, the call
     * will be retried repeatedly until the timeout is hit.
     *
     * @param timeout  how long to wait
     * @param timeUnit how long to wait (units)
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     * @param <T>      return type of the supplier
     * @return the result of the successful lambda expression call
     */
    public static <T> T retryUntilSuccess(final int timeout, final TimeUnit timeUnit, final UnreliableSupplier<T> lambda) {
        final int[] attempt = {0};
        final Exception[] lastException = {null};

        Future<T> retryThread = EXECUTOR_SERVICE.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                while (true) {
                    try {
                        return lambda.get();
                    } catch (Exception e) {
                        // Failed
                        LOGGER.trace("Retrying lambda call on attempt {}", attempt[0]++);
                        lastException[0] = e;
                    }
                }
            }
        });

        try {
            return retryThread.get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | java.util.concurrent.TimeoutException e) {
            if (lastException[0] != null) {
                throw new TimeoutException("Timeout waiting for result with exception", lastException[0]);
            } else {
                throw new TimeoutException(e);
            }
        }

    }

    /**
     * Call a callable repeatedly until it returns true. If an exception is thrown, the call
     * will be retried repeatedly until the timeout is hit.
     *
     * @param timeout  how long to wait
     * @param timeUnit how long to wait (units)
     * @param lambda   supplier lambda expression
     */
    public static void retryUntilTrue(final int timeout, final TimeUnit timeUnit, final Callable<Boolean> lambda) {
        retryUntilSuccess(timeout, timeUnit, new UnreliableSupplier<Object>() {
            @Override
            public Object get() throws Exception {
                if (!lambda.call()) {
                    throw new RuntimeException("Not ready yet");
                } else {
                    return null;
                }
            }
        });
    }
}
