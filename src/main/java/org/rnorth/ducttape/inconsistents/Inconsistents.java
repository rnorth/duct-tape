package org.rnorth.ducttape.inconsistents;

import org.jetbrains.annotations.NotNull;
import org.rnorth.ducttape.unreliables.Unreliables;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.rnorth.ducttape.Preconditions.check;

/**
 * Utility for calling a supplier that may take time to stabilise on a final result.
 */
public class Inconsistents {

    /**
     * Retry invocation of a supplier repeatedly until it returns a consistent result for a sufficient time period.
     *
     * This is intended for calls to components that take an unknown amount of time to stabilise, and where
     * repeated checks are the only way to detect that a stable state has been reached.
     *
     * @param consistentTime how long the result should be consistent for before it is returned
     * @param totalTimeout how long in total to wait for stabilisation to occur
     * @param timeUnit time unit for time intervals
     * @param lambda an UnreliableSupplier which should be called
     * @param <T> the return type of the UnreliableSupplier
     * @return the result of the supplier if it returned a consistent result for the specified interval
     */
    public static <T> T retryUntilConsistent(final int consistentTime, final int totalTimeout, @NotNull final TimeUnit timeUnit, @NotNull final Callable<T> lambda) {

        check("consistent time must be greater than 0", consistentTime > 0);
        check("total timeout must be greater than 0", totalTimeout > 0);

        long start = System.currentTimeMillis();

        Object[] recentValue = {null};
        long[] firstRecentValueTime = {0};
        long[] bestRun = {0};
        Object[] bestRunValue = {null};

        long consistentTimeInMillis = TimeUnit.MILLISECONDS.convert(consistentTime, timeUnit);

        return Unreliables.retryUntilSuccess(totalTimeout, timeUnit, () -> {
            T value = lambda.call();

            boolean valueIsSame = value == recentValue[0] || (value != null && value.equals(recentValue[0]));

            if (valueIsSame) {
                long now = System.currentTimeMillis();
                long timeSinceFirstValue = now - firstRecentValueTime[0];

                if (timeSinceFirstValue > bestRun[0]) {
                    bestRun[0] = timeSinceFirstValue;
                    bestRunValue[0] = value;
                }

                if (timeSinceFirstValue > consistentTimeInMillis) {
                    return value;
                }
            } else {
                // Reset everything and see if the next call yields the same result as this time
                recentValue[0] = value;
                firstRecentValueTime[0] = System.currentTimeMillis();
            }

            long timeSinceStart = System.currentTimeMillis() - start;

            if (bestRun[0] > 0) {
                throw new InconsistentResultsException(timeSinceStart, bestRunValue[0], bestRun[0]);
            } else {
                throw new ResultsNeverConsistentException(timeSinceStart);
            }
        });
    }
}
