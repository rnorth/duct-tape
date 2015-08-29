package org.rnorth.ducttape.circuitbreakers;

import org.jetbrains.annotations.NotNull;

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

    DefaultBreaker(@NotNull final TimeSource timeSource, final long autoResetInterval, @NotNull final TimeUnit autoResetUnit, @NotNull final StateStore stateStore) {

        this.timeSource = timeSource;
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
        this.stateStore = stateStore;
    }

    @Override
    public void tryDo(@NotNull final Runnable tryIfAlive, @NotNull final Runnable runOnFirstFailure, @NotNull final Runnable runIfBroken) {
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
    public void tryDo(@NotNull final Runnable tryIfAlive, @NotNull final Runnable runIfBroken) {
        tryDo(tryIfAlive, Breaker::NoOp, runIfBroken);
    }

    @Override
    public void tryDo(@NotNull final Runnable tryIfAlive) {
        tryDo(tryIfAlive, Breaker::NoOp, Breaker::NoOp);
    }

    @Override
    public <T> T tryGet(@NotNull final Callable<T> tryIfAlive, @NotNull final Runnable runOnFirstFailure, @NotNull final Supplier<T> getIfBroken) {
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
    public <T> T tryGet(@NotNull final Callable<T> tryIfAlive, @NotNull final Supplier<T> getIfBroken) {
        return tryGet(tryIfAlive, Breaker::NoOp, getIfBroken);
    }

    @Override
    public <T> Optional<T> tryGet(@NotNull final Callable<T> tryIfAlive) {
        return Optional.ofNullable(tryGet(tryIfAlive, Breaker::NoOp, () -> null));
    }

    @Override
    public State getState() {
        return this.stateStore.getState();
    }

    private boolean isBroken() {
        boolean broken = this.stateStore.getState() == State.BROKEN;
        boolean notAutoResetYet = (timeSource.getTimeMillis() - autoResetUnit.toMillis(autoResetInterval)) < this.stateStore.getLastFailure();
        return broken && notAutoResetYet;
    }

    private void setState(@NotNull final State state) {
        this.stateStore.setState(state);
    }

    private void setLastFailure(final long lastFailure) {
        this.stateStore.setLastFailure(lastFailure);
    }
}
