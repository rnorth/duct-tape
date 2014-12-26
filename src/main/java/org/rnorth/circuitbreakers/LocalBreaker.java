package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public class LocalBreaker implements Breaker {

    private State state = State.ALIVE;

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable runOnFirstFailure, Runnable doIfBroken) {
        if (state == State.BROKEN) {
            doIfBroken.run();
        } else {
            try {
                tryIfAlive.run();
            } catch (Exception e) {
                state = State.BROKEN;
                runOnFirstFailure.run();
                doIfBroken.run();
            }
        }
    }

    @Override
    public void tryDo(Runnable tryIfAlive, Runnable doIfBroken) {
        tryDo(tryIfAlive, Breaker::NoOp, doIfBroken);
    }

    @Override
    public void tryDo(Runnable tryIfAlive) {
        tryDo(tryIfAlive, Breaker::NoOp, Breaker::NoOp);
    }
}
