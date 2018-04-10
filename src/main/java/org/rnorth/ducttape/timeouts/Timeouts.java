package org.rnorth.ducttape.timeouts;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.rnorth.ducttape.Preconditions.check;

/**
 * Utilities to time out on slow running code.
 */
public class Timeouts {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(new ThreadFactory() {

        final AtomicInteger threadCounter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "ducttape-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    /**
     * Execute a lambda expression with a timeout. If it completes within the time, the result will be returned.
     * If it does not complete within the time, a TimeoutException will be thrown.
     * If it throws an exception, a RuntimeException wrapping that exception will be thrown.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     * @param <T>      return type of the lambda
     * @return the result of the successful lambda expression call
     */
    public static <T> T getWithTimeout(final int timeout, final TimeUnit timeUnit, @NotNull final Callable<T> lambda) {

        check("timeout must be greater than zero", timeout > 0);

        Future<T> future = EXECUTOR_SERVICE.submit(lambda);
        return callFuture(timeout, timeUnit, future);
    }

    /**
     * Execute a lambda expression with a timeout. If it completes within the time, the result will be returned.
     * If it does not complete within the time, a TimeoutException will be thrown.
     * If it throws an exception, a RuntimeException wrapping that exception will be thrown.
     *
     * @param timeout  how long to wait
     * @param timeUnit time unit for time interval
     * @param lambda   supplier lambda expression (may throw checked exceptions)
     */
    public static void doWithTimeout(final int timeout, @NotNull final TimeUnit timeUnit, @NotNull final Runnable lambda) {

        check("timeout must be greater than zero", timeout > 0);

        Future<?> future = EXECUTOR_SERVICE.submit(lambda);
        callFuture(timeout, timeUnit, future);
    }

    private static <T> T callFuture(final int timeout, @NotNull final TimeUnit timeUnit, @NotNull final Future<T> future) {
        try {
            return future.get(timeout, timeUnit);
        } catch (ExecutionException e) {
            // The cause of the ExecutionException is the actual exception that was thrown
            throw new RuntimeException(e.getCause());
        } catch (TimeoutException | InterruptedException e) {
            throw new org.rnorth.ducttape.TimeoutException(e);
        }
    }
}
