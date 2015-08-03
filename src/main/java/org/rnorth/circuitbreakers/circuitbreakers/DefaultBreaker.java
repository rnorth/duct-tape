package org.rnorth.circuitbreakers.circuitbreakers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author richardnorth
 */
class DefaultBreaker implements Breaker {

    private final TimeSource timeSource;
    private final long autoResetInterval;
    private final TimeUnit autoResetUnit;
    private final StateStore stateStore;

    DefaultBreaker(TimeSource timeSource, long autoResetInterval, TimeUnit autoResetUnit, StateStore stateStore) {

        this.timeSource = timeSource;
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
        this.stateStore = stateStore;
    }

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable runIfBroken) {
        if (isBroken()) {
            runIfBroken.run();
        } else {
            try {
                tryIfAlive.run();
                setState(State.OK);
            } catch (Exception e) {
                setState(State.BROKEN);
                setLastFailure(timeSource.getTimeMillis());
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
                setState(State.OK);
                return callResult;
            } catch (Exception e) {
                setState(State.BROKEN);
                setLastFailure(timeSource.getTimeMillis());
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
        return this.stateStore.getState();
    }

    private boolean isBroken() {
        return this.stateStore.getState() == State.BROKEN && (timeSource.getTimeMillis() - autoResetUnit.toMillis(autoResetInterval)) < this.stateStore.getLastFailure();
    }

    private void setState(State state) {
        this.stateStore.setState(state);
    }

    private void setLastFailure(long lastFailure) {
        this.stateStore.setLastFailure(lastFailure);
    }
}
