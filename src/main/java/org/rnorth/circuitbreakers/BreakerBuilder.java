package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public class BreakerBuilder {

    private BreakerBuilder() {}

    public static BreakerBuilder newBuilder() {
        return new BreakerBuilder();
    }

    public Breaker build() {
        return new LocalBreaker();
    }
}
