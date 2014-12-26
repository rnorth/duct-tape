package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public class SimpleStateStore implements StateStore {
    private Breaker.State state = Breaker.State.ALIVE;
    private long lastFailure;

    @Override
    public Breaker.State getState() {
        return state;
    }

    @Override
    public void setState(Breaker.State state) {
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
