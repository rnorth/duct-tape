package org.rnorth.circuitbreakers.circuitbreakers;

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
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public long getLastFailure() {
        return lastFailure;
    }

    @Override
    public void setLastFailure(long lastFailure) {
        this.lastFailure = lastFailure;
    }
}
