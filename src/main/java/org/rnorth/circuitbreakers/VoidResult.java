package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public abstract class VoidResult {

    public VoidResult onFail(Runnable onFailRunnable) {
        return this;
    }

    public void ifBroken(Runnable ifBrokenRunnable) {
        return;
    }

    public static class VoidSuccessResult extends VoidResult {

    }


    public static class VoidFailureResult extends VoidBrokenResult {
        public VoidResult onFail(Runnable onFailRunnable) {
            onFailRunnable.run();
            return new VoidFailureResult();
        }
    }

    public static class VoidBrokenResult extends VoidResult {

        public void ifBroken(Runnable ifBrokenRunnable) {
            ifBrokenRunnable.run();
        }

    }
}
