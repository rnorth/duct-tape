package org.rnorth.ducttape.inconsistents;

/**
 * Exception caused by a failure to obtain consistent results.
 */
public class InconsistentResultsException extends ResultsNeverConsistentException {

    protected final Object mostConsistentValue;
    protected final long mostConsistentTime;

    public InconsistentResultsException(long timeSinceStart, Object mostConsistentValue, long mostConsistentTime) {
        super("After " + timeSinceStart + "ms, results have not become consistent. Most consistent value was " + mostConsistentValue + ", seen for " + mostConsistentTime + "ms", timeSinceStart);
        this.mostConsistentValue = mostConsistentValue;
        this.mostConsistentTime = mostConsistentTime;
    }

    public Object getMostConsistentValue() {
        return mostConsistentValue;
    }

    public long getMostConsistentTime() {
        return mostConsistentTime;
    }
}
