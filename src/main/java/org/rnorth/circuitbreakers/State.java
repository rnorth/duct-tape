package org.rnorth.circuitbreakers;

/**
* @author richardnorth
*/
public enum State {
    /**
     * The breaker is alive, i.e. trying to perform requested primary actions.
     */
    ALIVE,
    /**
     * The breaker is broken, i.e. avoiding calling primary actions, and falling straight through to the fallback actions.
     */
    BROKEN
}
