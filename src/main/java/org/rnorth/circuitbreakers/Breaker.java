package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public interface Breaker {

    void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable doIfBroken);

    void tryDo(Runnable tryIfAlive, Runnable doIfBroken);

    void tryDo(Runnable tryIfAlive);

    static void NoOp() {
        // No-op
    }

    public enum State {
        ALIVE, BROKEN
    }
}
