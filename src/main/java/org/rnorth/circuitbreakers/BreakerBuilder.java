package org.rnorth.circuitbreakers;

import java.util.concurrent.TimeUnit;

/**
 * @author richardnorth
 */
public class BreakerBuilder {

    private TimeSource timeSource = new TimeSource();
    private long autoResetInterval = Long.MAX_VALUE;
    private TimeUnit autoResetUnit = TimeUnit.DAYS;

    private BreakerBuilder() {}

    public static BreakerBuilder newBuilder() {
        return new BreakerBuilder();
    }

    public Breaker build() {
        LocalBreaker breaker = new LocalBreaker(timeSource, autoResetInterval, autoResetUnit);
        return breaker;
    }

    public BreakerBuilder timeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    public BreakerBuilder autoResetAfter(long autoResetInterval, TimeUnit autoResetUnit) {
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
        return this;
    }
}
