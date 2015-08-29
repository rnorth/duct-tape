package org.rnorth.circuitbreakers.circuitbreakers;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author richardnorth
 */
class MapBackedStateStore implements StateStore {
    private final Map<String, Object> map;
    private final String keyPrefix;

    public MapBackedStateStore(@NotNull final Map<String, Object> map, @NotNull final String keyPrefix) {
        this.map = map;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public State getState() {
        return (State) this.map.getOrDefault(keyPrefix + "_STATE", State.OK);
    }

    @Override
    public void setState(@NotNull final State state) {
        this.map.put(keyPrefix + "_STATE", state);
    }

    @Override
    public long getLastFailure() {
        return (long) this.map.getOrDefault(keyPrefix + "_LAST_FAILURE", 0L);
    }

    @Override
    public void setLastFailure(final long lastFailure) {
        this.map.put(keyPrefix + "_LAST_FAILURE", lastFailure);
    }
}
