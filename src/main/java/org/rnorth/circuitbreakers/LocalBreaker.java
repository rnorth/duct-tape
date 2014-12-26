package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public class LocalBreaker implements Breaker {

    private State state = State.ALIVE;

    @Override
    public VoidResult tryDo(Runnable runnable) {

        if (state == State.BROKEN) {
            return new VoidResult.VoidBrokenResult();
        }

        try {
            runnable.run();
            return new VoidResult.VoidSuccessResult();
        } catch (Exception e) {
            state = State.BROKEN;
            return new VoidResult.VoidFailureResult();
        }
    }
}
