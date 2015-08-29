package org.rnorth.ducttape.circuitbreakers;

import org.jetbrains.annotations.NotNull;

/**
 * An external store for state which a {@link Breaker} can use.
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
    void setState(@NotNull State state);

    long getLastFailure();
    void setLastFailure(long lastFailure);

}
