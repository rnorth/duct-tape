package org.rnorth.circuitbreakers.inconsistents;

import org.rnorth.circuitbreakers.unreliables.UnreliableSupplier;
import org.rnorth.circuitbreakers.unreliables.Unreliables;

import java.util.concurrent.TimeUnit;

/**
 * Created by rnorth on 23/07/2015.
 */
public class Inconsistents {

    public static <T> T retryUntilConsistent(int consistentTime, int totalTimeout, TimeUnit timeUnit, UnreliableSupplier<T> lambda) {

        long start = System.currentTimeMillis();

        Object[] recentValue = {null};
        long[] firstRecentValueTime = {0};
        long[] bestRun = {0};
        Object[] bestRunValue = {null};

        long consistentTimeInMillis = timeUnit.convert(consistentTime, TimeUnit.MILLISECONDS);

        return Unreliables.retryUntilSuccess(totalTimeout, timeUnit, new UnreliableSupplier<T>() {
            @Override
            public T get() throws Exception {
                T value = lambda.get();

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
            }
        });
    }
}
