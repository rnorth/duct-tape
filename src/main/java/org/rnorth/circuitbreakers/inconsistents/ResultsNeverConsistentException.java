package org.rnorth.circuitbreakers.inconsistents;

/**
 * Created by rnorth on 23/07/2015.
 */
public class ResultsNeverConsistentException extends RuntimeException {

    protected final long timeSinceStart;

    public ResultsNeverConsistentException(String message, long timeSinceStart) {
        super(message);
        this.timeSinceStart = timeSinceStart;
    }

    public ResultsNeverConsistentException(long timeSinceStart) {
        super("After " + timeSinceStart + "ms, results have not become consistent. The value was different every time!");
        this.timeSinceStart = timeSinceStart;
    }

    public long getTimeSinceStart() {
        return timeSinceStart;
    }
}
