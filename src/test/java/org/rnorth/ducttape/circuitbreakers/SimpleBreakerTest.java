package org.rnorth.ducttape.circuitbreakers;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author richardnorth
 */
public class SimpleBreakerTest {

    private List<Call> doCalls;

    @Before
    public void setup() {
        doCalls = new ArrayList<>();
    }

    @Test
    public void testDoFailBrokenMethods() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        IntStream.range(0, 5).forEach(j ->

                        breaker.tryDo(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                        }, this::onFailCalled, this::ifBrokenCalled)

        );

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO, Call.FAILED, Call.BROKEN, // Fail on the third iteration, calling fail handler and broken handler too
                Call.BROKEN, // Just call broken handler
                Call.BROKEN), doCalls);
    }

    @Test
    public void testDoBrokenMethods() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        IntStream.range(0, 5).forEach(j ->

                        breaker.tryDo(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                        }, this::ifBrokenCalled)

        );

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO, Call.BROKEN, // Fail on the third iteration, calling broken handler too
                Call.BROKEN, // Just call broken handler
                Call.BROKEN), doCalls);
    }

    @Test
    public void testJustDoMethod() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        IntStream.range(0, 5).forEach(j ->

                        breaker.tryDo(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                        })

        );

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO), doCalls);
    }

    @Test
    public void testGetFailBrokenMethods() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        List gets = asList(IntStream.range(0, 5).mapToObj(j ->

                        breaker.tryGet(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                            return "GET" + j;
                        }, this::onFailCalled, () -> {
                            ifBrokenCalled();
                            return "BROKEN" + j;
                        })

        ).toArray());

        assertEquals(asList(
                "GET0",
                "GET1",
                "BROKEN2", // Fail on the third iteration, with only the failure return result
                "BROKEN3", // Just call broken handler
                "BROKEN4"), gets);

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO, Call.FAILED, Call.BROKEN, // Fail on the third iteration, calling fail handler and broken handler too
                Call.BROKEN, // Just call broken handler
                Call.BROKEN), doCalls);
    }

    @Test
    public void testGetBrokenMethods() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        List gets = asList(IntStream.range(0, 5).mapToObj(j ->

                        breaker.tryGet(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                            return "GET" + j;
                        }, () -> {
                            ifBrokenCalled();
                            return "BROKEN" + j;
                        })

        ).toArray());

        assertEquals(asList(
                "GET0",
                "GET1",
                "BROKEN2", // Fail on the third iteration, with only the failure return result
                "BROKEN3", // Just call broken handler
                "BROKEN4"), gets);

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO, Call.BROKEN, // Fail on the third iteration, calling fail handler and broken handler too
                Call.BROKEN, // Just call broken handler
                Call.BROKEN), doCalls);
    }

    @Test
    public void testJustGetMethod() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        List gets = asList(IntStream.range(0, 5).mapToObj(j ->

                        breaker.tryGet(() -> {
                            doCalled();
                            if (j == 2) throw new RuntimeException();
                            return "GET" + j;
                        })

        ).toArray());

        assertEquals(asList(
                Optional.of("GET0"),
                Optional.of("GET1"),
                Optional.empty(), // Fail on the third iteration, with only the failure return result
                Optional.empty(), // Just call broken handler
                Optional.empty()), gets);

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO), doCalls);
    }

    private void ifBrokenCalled() {
        doCalls.add(Call.BROKEN);
    }

    private void onFailCalled() {
        doCalls.add(Call.FAILED);
    }

    private void doCalled() {
        doCalls.add(Call.DO);
    }

    private enum Call {
        DO, FAILED, BROKEN
    }
}
