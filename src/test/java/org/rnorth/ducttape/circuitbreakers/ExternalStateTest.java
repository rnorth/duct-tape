package org.rnorth.ducttape.circuitbreakers;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertNotEquals;

/**
 * @author richardnorth
 */
public class ExternalStateTest {

    @Test
    public void testUseOfStateStore() {

        StateStore mockStateStore = mock(StateStore.class);

        Breaker breaker = BreakerBuilder.newBuilder()
                .timeSource(new TimeSource.DummyTimeSource(10)) // 'now' is always 10
                .autoResetAfter(1, TimeUnit.MILLISECONDS)
                .storeStateIn(mockStateStore)
                .build();

        when(mockStateStore.getState()).thenReturn(State.OK);
        assertEquals("When the state store reports state is OK, the first supplier is called", "called", breaker.tryGet(() -> "called").get());

        when(mockStateStore.getState()).thenReturn(State.BROKEN);
        when(mockStateStore.getLastFailure()).thenReturn(10L);
        assertEquals("When the state store reports state is BROKEN, the fallback supplier is called", "not called", breaker.tryGet(() -> "called", () -> "not called"));

        when(mockStateStore.getState()).thenReturn(State.BROKEN);
        when(mockStateStore.getLastFailure()).thenReturn(9L);
        assertEquals("When the state store reports state is BROKEN but expired, the first supplier is called", "called", breaker.tryGet(() -> "called").get());

        verify(mockStateStore);
    }

    @Test
    public void testMapStateStore() {
        Map<String, Object> map = new HashMap<>();

        MapBackedStateStore store = new MapBackedStateStore(map, "TEST");

        assertEquals("The state store defaults to OK", State.OK, store.getState()); // initial state

        store.setState(State.BROKEN);
        assertEquals("The state store can be set to BROKEN", State.BROKEN, store.getState());

        store.setState(State.OK);
        assertEquals("The state store can be set to OK", State.OK, store.getState());

        store.setLastFailure(666L);
        assertEquals("The state store last failure time can be set", 666L, store.getLastFailure());

        MapBackedStateStore otherStoreUsingSameMap = new MapBackedStateStore(map, "ANOTHERPREFIX");
        store.setLastFailure(444L);
        assertNotEquals("The state store can hold more than one last failure time", 444L, otherStoreUsingSameMap.getLastFailure());
        otherStoreUsingSameMap.setState(State.OK);
        store.setState(State.BROKEN);
        assertNotEquals("The state store stores a separate state for each breaker prefix", State.BROKEN, otherStoreUsingSameMap.getState());
    }

    @Test
    public void testBuilderUsingMapBackedStore() {

        ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

        Breaker breaker = BreakerBuilder.newBuilder()
                        .storeStateIn(map, "PREFIX")
                        .build();

        assertEquals("The state store is not used before the breaker is called", 0, map.size());

        breaker.tryDo(() -> { throw new RuntimeException(); });

        assertEquals("The state store is used when the breaker is called", 2, map.size());
    }
}
