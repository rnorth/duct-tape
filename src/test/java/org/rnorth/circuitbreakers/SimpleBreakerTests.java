package org.rnorth.circuitbreakers;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author richardnorth
 */
public class SimpleBreakerTests {

    private List<Call> doCalls;

    @Before
    public void setup() {
        doCalls = new ArrayList<>();
    }

    @Test
    public void simpleTest() {
        Breaker breaker = BreakerBuilder.newBuilder().build();

        assertNotNull(breaker);

        IntStream.range(0, 5).forEach(j ->

            breaker.tryDo(() -> {
                doCalled();
                if (j == 2) throw new RuntimeException();
            }).onFail(() -> {
                onFailCalled();
            }).ifBroken(() -> {
                ifBrokenCalled();
            })

        );

        assertEquals(asList(
                Call.DO,
                Call.DO,
                Call.DO, Call.FAILED, Call.BROKEN, // Fail on the third iteration, calling fail handler and broken handler too
                Call.BROKEN, // Just call broken handler
                Call.BROKEN), doCalls);
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
