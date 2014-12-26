package org.rnorth.circuitbreakers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author richardnorth
 */
public class LocalBreaker implements Breaker {

    private final TimeSource timeSource;
    private final long autoResetInterval;
    private final TimeUnit autoResetUnit;
    private State state = State.ALIVE;
    private long lastFailure;

    LocalBreaker(TimeSource timeSource, long autoResetInterval, TimeUnit autoResetUnit) {

        this.timeSource = timeSource;
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
    }

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable runIfBroken) {
        if (isBroken()) {
            runIfBroken.run();
        } else {
            try {
                tryIfAlive.run();
                state = State.ALIVE;
            } catch (Exception e) {
                state = State.BROKEN;
                lastFailure = timeSource.getTimeMillis();
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
        if (isBroken()) {
            return getIfBroken.get();
        } else {
            try {
                T callResult = tryIfAlive.call();
                state = State.ALIVE;
                return callResult;
            } catch (Exception e) {
                state = State.BROKEN;
                lastFailure = timeSource.getTimeMillis();
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

    @Override
    public State getState() {
        return state;
    }

    private boolean isBroken() {
        return state == State.BROKEN && ( timeSource.getTimeMillis() - autoResetUnit.toMillis(autoResetInterval)) < lastFailure;
    }
}
