package org.rnorth.circuitbreakers;

/**
 * An external store for state which a {@link org.rnorth.circuitbreakers.Breaker} can use.
 *
 * While a default instance of Breaker will use a simple object local to the current JVM, alternative instances of `StateStore`
 * could be created to maintain state somewhere else, e.g.:
 *
 * * A persistent store on disk. This could be helpful to maintain breaker state between runs of the JVM.
 * * A distributed store, such as a cache shared across a cluster of machines. This could be helpful if breaker state
 *    needs to be shared by many machines.
 *
 * @author richardnorth
 */
public interface StateStore {

    State getState();
    void setState(State state);

    long getLastFailure();
    void setLastFailure(long lastFailure);

}
