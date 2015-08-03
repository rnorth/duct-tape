package org.rnorth.circuitbreakers.circuitbreakers;

/**
* @author richardnorth
*/
public enum State {
    /**
     * The breaker is OK, i.e. trying to perform requested primary actions.
     */
    OK,
    /**
     * The breaker is broken, i.e. avoiding calling primary actions, and falling straight through to the fallback actions.
     */
    BROKEN
}
