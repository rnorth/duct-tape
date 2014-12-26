package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public interface Breaker {
    VoidResult tryDo(Runnable runnable);

    public enum State {
        ALIVE, BROKEN
    }
}
