package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public interface StateStore {

    Breaker.State getState();
    void setState(Breaker.State state);

    long getLastFailure();
    void setLastFailure(long lastFailure);

}
