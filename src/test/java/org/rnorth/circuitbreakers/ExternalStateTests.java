package org.rnorth.circuitbreakers;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * @author richardnorth
 */
public class ExternalStateTests {

    @Test
    public void testUseOfStateStore() {

        StateStore mockStateStore = mock(StateStore.class);

        Breaker breaker = BreakerBuilder.newBuilder()
                .timeSource(new TimeSource.DummyTimeSource(10)) // 'now' is always 10
                .autoResetAfter(1, TimeUnit.MILLISECONDS)
                .storeStateIn(mockStateStore)
                .build();

        when(mockStateStore.getState()).thenReturn(State.ALIVE);
        assertEquals("called", breaker.tryGet(() -> "called").get());

        when(mockStateStore.getState()).thenReturn(State.BROKEN);
        when(mockStateStore.getLastFailure()).thenReturn(10L);
        assertEquals("not called", breaker.tryGet(() -> "called", () -> "not called"));

        when(mockStateStore.getState()).thenReturn(State.BROKEN);
        when(mockStateStore.getLastFailure()).thenReturn(9L);
        assertEquals("called", breaker.tryGet(() -> "called").get());

        verify(mockStateStore);
    }

    @Test
    public void testMapStateStore() {
        Map<String, Object> map = new HashMap<>();

        MapBackedStateStore store = new MapBackedStateStore(map, "TEST");

        assertEquals(State.ALIVE, store.getState()); // initial state

        store.setState(State.BROKEN);
        assertEquals(State.BROKEN, store.getState());

        store.setState(State.ALIVE);
        assertEquals(State.ALIVE, store.getState());

        store.setLastFailure(666L);
        assertEquals(666L, store.getLastFailure());

        MapBackedStateStore otherStoreUsingSameMap = new MapBackedStateStore(map, "ANOTHERPREFIX");
        store.setLastFailure(444L);
        assertNotEquals(444L, otherStoreUsingSameMap.getLastFailure());
        otherStoreUsingSameMap.setState(State.ALIVE);
        store.setState(State.BROKEN);
        assertNotEquals(State.BROKEN, otherStoreUsingSameMap.getState());
    }

    @Test
    public void testBuilderUsingMapBackedStore() {

        ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

        Breaker breaker = BreakerBuilder.newBuilder()
                        .storeStateIn(map, "PREFIX")
                        .build();

        assertEquals(0, map.size());

        breaker.tryDo(() -> { throw new RuntimeException(); });

        assertEquals(2, map.size());
    }
}
