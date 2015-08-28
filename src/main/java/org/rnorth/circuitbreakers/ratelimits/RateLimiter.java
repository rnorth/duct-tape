package org.rnorth.circuitbreakers.ratelimits;

import java.util.concurrent.Callable;

/**
 * Base class for rate limiters. Use RateLimiterBuilder to build new instances.
 */
public abstract class RateLimiter {

    protected long lastInvocation;

    /**
     * Invoke a lambda function, with Thread.sleep() being called to limit the execution rate if needed.
     * @param lambda a Runnable lamda function to invoke
     * @throws Exception rethrown from lambda
     */
    public void invokeWhenReady(Runnable lambda) throws Exception {

        // Wait before proceeding, if needed
        long waitBeforeNextInvocation = getWaitBeforeNextInvocation();
        try {
            Thread.sleep(waitBeforeNextInvocation);
        } catch (InterruptedException ignored) { }

        try {
            lambda.run();
        } finally {
            lastInvocation = System.currentTimeMillis();
        }


    }

    /**
     *
     * Invoke a lambda function and get the result, with Thread.sleep() being called to limit the execution rate
     * if needed.
     * @param lambda a Callable lamda function to invoke
     * @param <T> return type of the lamda
     * @throws Exception rethrown from lambda
     */
    public <T> T invokeWhenReady(Callable<T> lambda) throws Exception {

        // Wait before proceeding, if needed
        long waitBeforeNextInvocation = getWaitBeforeNextInvocation();
        try {
            Thread.sleep(waitBeforeNextInvocation);
        } catch (InterruptedException ignored) { }

        try {
            return lambda.call();
        } finally {
            lastInvocation = System.currentTimeMillis();
        }
    }

    protected abstract long getWaitBeforeNextInvocation();
}
