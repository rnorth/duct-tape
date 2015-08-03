package org.rnorth.circuitbreakers.circuitbreakers;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * BreakerBuilder
 *
 * A builder of {@link Breaker} instances having any combination of the following features:
 * <ul>
 * <li>automatic reset after a configured period of time has passed since the last failure</li>
 * <li>configurable state storage, for example for use with a distributed map implementation</li>
 * </ul>
 * These features are optional; by default instances will be created with none of them.
 *
 * @author richardnorth
 */
public class BreakerBuilder {

    private TimeSource timeSource = new TimeSource();
    private long autoResetInterval = Long.MAX_VALUE;
    private TimeUnit autoResetUnit = TimeUnit.DAYS;
    private StateStore stateStore = new SimpleStateStore();

    private BreakerBuilder() {
    }

    /**
     * @return a new `BreakerBuilder` instance.
     */
    public static BreakerBuilder newBuilder() {
        return new BreakerBuilder();
    }

    /**
     * @return a {@link Breaker} instance configured using settings passed in to this
     * `BreakerBuilder`
     */
    public Breaker build() {
        DefaultBreaker breaker = new DefaultBreaker(timeSource, autoResetInterval, autoResetUnit, stateStore);
        return breaker;
    }

    /**
     * Use a {@link TimeSource} instance to track time elapsed since last failure.
     * Mainly intended for use in testing.
     *
     * @param timeSource a time source instance to track time elapsed since last failure.
     *
     * @return this
     */
    public BreakerBuilder timeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    /**
     * Configure the breaker to automatically reset a given time after a failure has occurred. Use this for
     * unattended retry behaviour.
     *
     * If this method is <i>not</i> used, the default is for the breaker to wait `Long.MAX_VALUE` days before it
     * resets automatically, i.e. effectively forever.
     *
     * @param autoResetInterval the interval length
     * @param autoResetUnit     the units of the interval
     *
     * @return this
     */
    public BreakerBuilder autoResetAfter(long autoResetInterval, TimeUnit autoResetUnit) {
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
        return this;
    }

    /**
     * Configure the breaker to use the provided {@link java.util.Map} to store its state. The <pre>keyPrefix</pre> is used to
     * uniquely identify the breaker's entries in the map.
     *
     * <pre>keyPrefix</pre> should be unique; behaviour is undefined if it is not. Additionally, behaviour is undefined if entries
     * are directly modified.
     *
     * @param map       the map to use for storage
     * @param keyPrefix a unique prefix for the +Breaker+'s entries in the map
     *
     * @return this
     */
    public BreakerBuilder storeStateIn(ConcurrentMap<String, Object> map, String keyPrefix) {
        this.stateStore = new MapBackedStateStore(map, keyPrefix);
        return this;
    }

    /**
     * Configure the breaker to use the provided {@link StateStore} to store its state.
     *
     * @param stateStore       an instance of {@link StateStore} to store state in
     *
     * @return this
     */
    public BreakerBuilder storeStateIn(StateStore stateStore) {
        this.stateStore = stateStore;
        return this;
    }
}
