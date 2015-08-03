package org.rnorth.circuitbreakers.inconsistents;

/**
 * Created by rnorth on 23/07/2015.
 */
public class InconsistentResultsException extends ResultsNeverConsistentException {

    protected Object mostConsistentValue;
    protected long mostConsistentTime;

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
