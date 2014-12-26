package org.rnorth.circuitbreakers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author richardnorth
 */
public class BreakerBuilder {

    private TimeSource timeSource = new TimeSource();
    private long autoResetInterval = Long.MAX_VALUE;
    private TimeUnit autoResetUnit = TimeUnit.DAYS;
    private StateStore stateStore = new SimpleStateStore();

    private BreakerBuilder() {
    }

    public static BreakerBuilder newBuilder() {
        return new BreakerBuilder();
    }

    public Breaker build() {
        LocalBreaker breaker = new LocalBreaker(timeSource, autoResetInterval, autoResetUnit, stateStore);
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

    public BreakerBuilder storeStateIn(Map<String, Object> map, String keyPrefix) {
        this.stateStore = new MapBackedStateStore(map, keyPrefix);
        return this;
    }

    public BreakerBuilder storeStateIn(StateStore stateStore) {
        this.stateStore = stateStore;
        return this;
    }
}
