package org.rnorth.circuitbreakers;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * = BreakerBuilder
 *
 * A builder of {@link org.rnorth.circuitbreakers.Breaker} instances having any combination of the following features:
 *
 * * automatic reset after a configured period of time has passed since the last failure
 * * configurable state storage, for example for use with a distributed map implementation
 *
 * These features are optional; by default instances will be created with none of them.
 *
 * [source,java]
 * .Simple uncustomized usage
 * --
 * include::{projectDir}/src/test/java/org/rnorth/circuitbreakers/ExampleTests.java[tags=ExampleSimpleBuild,indent=0]
 * --
 *
 * [source,java]
 * .Building a breaker which automatically resets after a period of time since the last failure
 * --
 * include::{projectDir}/src/test/java/org/rnorth/circuitbreakers/ExampleTests.java[tags=ExampleAutoResetBuild,indent=0]
 * --
 *
 * [source,java]
 * .Building a breaker which stores its state in an external map
 * --
 * include::{projectDir}/src/test/java/org/rnorth/circuitbreakers/ExampleTests.java[tags=ExampleExternalStoreBuild,indent=0]
 * --
 *
 * @author https://github.com/rnorth[Richard North]
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
     * @return a {@link org.rnorth.circuitbreakers.Breaker} instance configured using settings passed in to this
     * `BreakerBuilder`
     */
    public Breaker build() {
        DefaultBreaker breaker = new DefaultBreaker(timeSource, autoResetInterval, autoResetUnit, stateStore);
        return breaker;
    }

    /**
     * Use a {@link org.rnorth.circuitbreakers.TimeSource} instance to track time elapsed since last failure.
     * Mainly intended for use in testing.
     */
    public BreakerBuilder timeSource(TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    /**
     * Configure the breaker to automatically reset a given time after a failure has occurred. Use this for
     * unattended retry behaviour.
     *
     * If this method is _not_ used, the default is for the breaker to wait `Long.MAX_VALUE` days before it
     * resets automatically, i.e. effectively forever.
     *
     * @param autoResetInterval the interval length
     * @param autoResetUnit     the units of the interval
     */
    public BreakerBuilder autoResetAfter(long autoResetInterval, TimeUnit autoResetUnit) {
        this.autoResetInterval = autoResetInterval;
        this.autoResetUnit = autoResetUnit;
        return this;
    }

    /**
     * Configure the breaker to use the provided {@link java.util.Map} to store its state. The `keyPrefix` is used to
     * uniquely identify the breaker's entries in the map.
     *
     * `keyPrefix` should be unique; behaviour is undefined if it is not. Additionally, behaviour is undefined if entries
     * are directly modified.
     *
     * @param map       the map to use for storage
     * @param keyPrefix a unique prefix for the +Breaker+'s entries in the map
     */
    public BreakerBuilder storeStateIn(ConcurrentMap<String, Object> map, String keyPrefix) {
        this.stateStore = new MapBackedStateStore(map, keyPrefix);
        return this;
    }

    /**
     * Configure the breaker to use the provided {@link org.rnorth.circuitbreakers.StateStore} to store its state.
     *
     * @param stateStore       an instance of {@link org.rnorth.circuitbreakers.StateStore} to store state in
     */
    public BreakerBuilder storeStateIn(StateStore stateStore) {
        this.stateStore = stateStore;
        return this;
    }
}
