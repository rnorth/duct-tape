package org.rnorth.circuitbreakers.inconsistents;

/**
 * Exception caused by a failure to obtain consistent results.
 */
public class ResultsNeverConsistentException extends RuntimeException {

    protected final long timeSinceStart;

    public ResultsNeverConsistentException(String message, long timeSinceStart) {
        super(message);
        this.timeSinceStart = timeSinceStart;
    }

    public ResultsNeverConsistentException(long timeSinceStart) {
        super("After " + timeSinceStart + "ms, results have not become consistent. The value was never consistent");
        this.timeSinceStart = timeSinceStart;
    }

    public long getTimeSinceStart() {
        return timeSinceStart;
    }
}
