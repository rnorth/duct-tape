package org.rnorth.circuitbreakers;

import java.util.Map;

/**
 * @author richardnorth
 */
public class MapBackedStateStore implements StateStore {
    private final Map<String, Object> map;
    private final String keyPrefix;

    public MapBackedStateStore(Map<String, Object> map, String keyPrefix) {
        this.map = map;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public Breaker.State getState() {
        return (Breaker.State) this.map.getOrDefault(keyPrefix + "_STATE", Breaker.State.ALIVE);
    }

    @Override
    public void setState(Breaker.State state) {
        this.map.put(keyPrefix + "_STATE", state);
    }

    @Override
    public long getLastFailure() {
        return (long) this.map.getOrDefault(keyPrefix + "_LAST_FAILURE", 0L);
    }

    @Override
    public void setLastFailure(long lastFailure) {
        this.map.put(keyPrefix + "_LAST_FAILURE", lastFailure);
    }
}
