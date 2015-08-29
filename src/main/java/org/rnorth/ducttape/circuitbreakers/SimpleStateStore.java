package org.rnorth.ducttape.circuitbreakers;

import org.jetbrains.annotations.NotNull;

/**
 * @author richardnorth
 */
class SimpleStateStore implements StateStore {
    private State state = State.OK;
    private long lastFailure;

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(@NotNull final State state) {
        this.state = state;
    }

    @Override
    public long getLastFailure() {
        return lastFailure;
    }

    @Override
    public void setLastFailure(final long lastFailure) {
        this.lastFailure = lastFailure;
    }
}
