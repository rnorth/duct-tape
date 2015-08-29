package org.rnorth.ducttape.circuitbreakers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A Circuit Breaker, per http://martinfowler.com/bliki/CircuitBreaker.html[the pattern of the same name].
 *
 * @author richardnorth
 */
public interface Breaker {

    /**
     * Do something, unless the breaker is in a broken state, in which case perform a fallback action.
     *
     * @param tryIfAlive            A runnable, to be invoked unless the breaker is in `BROKEN` state
     * @param runOnFirstFailure     A runnable, to be invoked immediately if the first runnable fails
     * @param runIfBroken           A runnable, to be invoked if the breaker is in `BROKEN` state
     */
    void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable runIfBroken);

    /**
     * Do something, unless the breaker is in a broken state, in which case perform a fallback action.
     *
     * @param tryIfAlive            A runnable, to be invoked unless the breaker is in `BROKEN` state
     * @param runIfBroken           A runnable, to be invoked if the breaker is in `BROKEN` state
     */
    void tryDo(Runnable tryIfAlive, Runnable runIfBroken);

    /**
     * Do something, unless the breaker is in a broken state, in which case do nothing.
     *
     * @param tryIfAlive            A runnable, to be invoked unless the breaker is in `BROKEN` state
     */
    void tryDo(Runnable tryIfAlive);

    /**
     * Get the result of something, unless the breaker is in a broken state, in which case get a fallback value.
     *
     * @param tryIfAlive            A callable, to be called unless the breaker is in `BROKEN` state. May throw a checked exception.
     * @param runOnFirstFailure     A runnable, to be invoked immediately if the first callable fails
     * @param getIfBroken           A supplier, to be invoked if the breaker is in `BROKEN` state. May not throw a checked exception.
     * @param <T>                   The type to be returned by the `tryIfAlive` callable and `getIfBroken` supplier
     * @return                      The value returned by either `tryIfAlive` or `getIfBroken`
     */
    <T> T tryGet(Callable<T> tryIfAlive, Runnable runOnFirstFailure, Supplier<T> getIfBroken);

    /**
     * Get the result of something, unless the breaker is in a broken state, in which case get a fallback value.
     *
     * @param tryIfAlive            A callable, to be called unless the breaker is in `BROKEN` state. May throw a checked exception.
     * @param getIfBroken           A supplier, to be invoked if the breaker is in `BROKEN` state. May not throw a checked exception.
     * @param <T>                   The type to be returned by the `tryIfAlive` callable and `getIfBroken` supplier
     * @return                      The value returned by either `tryIfAlive` or `getIfBroken`
     */
    <T> T tryGet(Callable<T> tryIfAlive, Supplier<T> getIfBroken);

    /**
     * Get the result of something, unless the breaker is in a broken state, in which case get a fallback value.
     *
     * @param tryIfAlive            A callable, to be called unless the breaker is in `BROKEN` state. May throw a checked exception.
     * @param <T>                   The type to be returned by the `tryIfAlive` callable and `getIfBroken` supplier
     * @return                      An `Optional` wrapping either the value returned by `tryIfAlive`, or empty if the breaker is in
     *                                  `BROKEN` state or `tryIfAlive` failed
     */
    <T> Optional<T> tryGet(Callable<T> tryIfAlive);

    /**
     * @return                      The current state of the breaker
     */
    State getState();

    static void NoOp() {
        // No-op
    }
}
