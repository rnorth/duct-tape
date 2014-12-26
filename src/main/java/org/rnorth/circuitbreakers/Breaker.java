package org.rnorth.circuitbreakers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author richardnorth
 */
public interface Breaker {

    void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable runIfBroken);

    void tryDo(Runnable tryIfAlive, Runnable runIfBroken);

    void tryDo(Runnable tryIfAlive);

    static void NoOp() {
        // No-op
    }

    <T> T tryGet(Callable<T> tryIfAlive, Runnable runOnFirstFailure, Supplier<T> getIfBroken);

    <T> T tryGet(Callable<T> tryIfAlive, Supplier<T> getIfBroken);

    <T> Optional<T> tryGet(Callable<T> tryIfAlive);

    State getState();

    public enum State {
        ALIVE, BROKEN
    }
}
