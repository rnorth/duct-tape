package org.rnorth.circuitbreakers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author richardnorth
 */
public class LocalBreaker implements Breaker {

    private State state = State.ALIVE;

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable runIfBroken) {
        if (state == State.BROKEN) {
            runIfBroken.run();
        } else {
            try {
                tryIfAlive.run();
            } catch (Exception e) {
                state = State.BROKEN;
                runOnFirstFailure.run();
                runIfBroken.run();
            }
        }
    }

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable runIfBroken) {
        tryDo(tryIfAlive, Breaker::NoOp, runIfBroken);
    }

    @Override
    public void tryDo(Runnable tryIfAlive) {
        tryDo(tryIfAlive, Breaker::NoOp, Breaker::NoOp);
    }

    @Override
    public <T> T tryGet(Callable<T> tryIfAlive, Runnable runOnFirstFailure, Supplier<T> getIfBroken) {
        if (state == State.BROKEN) {
            return getIfBroken.get();
        } else {
            try {
                return tryIfAlive.call();
            } catch (Exception e) {
                state = State.BROKEN;
                runOnFirstFailure.run();
                return getIfBroken.get();
            }
        }
    }

    @Override
    public <T> T tryGet(Callable<T> tryIfAlive, Supplier<T> getIfBroken) {
        return tryGet(tryIfAlive, Breaker::NoOp, getIfBroken);
    }

    @Override
    public <T> Optional<T> tryGet(Callable<T> tryIfAlive) {
        return Optional.ofNullable(tryGet(tryIfAlive, Breaker::NoOp, () -> null));
    }
}
