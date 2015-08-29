package org.rnorth.ducttape.timeouts;

import org.junit.Test;
import org.rnorth.ducttape.TimeoutException;

import java.util.concurrent.TimeUnit;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertThrows;

/**
 * Tests for Timeouts class.
 */
public class TimeoutsTest {

    @Test
    public void timeoutThrowsException() {
        assertThrows("It throws a TimeoutException if execution time is exceeded", TimeoutException.class, () -> {
            Timeouts.doWithTimeout(1, TimeUnit.SECONDS, () -> {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) { }
            });
        });
    }

    @Test
    public void withinTimeIsOk() {
        Timeouts.doWithTimeout(1, TimeUnit.SECONDS, () -> {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ignored) { }
        });
    }

    @Test
    public void timeoutThrowsExceptionWithoutReturnValue() {
        assertThrows("It throws a TimeoutException if execution time is exceeded", TimeoutException.class, () -> {
            Timeouts.getWithTimeout(1, TimeUnit.SECONDS, () -> {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) { }
                return "result";
            });
        });
    }

    @Test
    public void withinTimeIsOkAndCanReturnResult() {
        String result = Timeouts.getWithTimeout(1, TimeUnit.SECONDS, () -> {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException ignored) { }
            return "result";
        });

        assertEquals("A result is returned from the lambda", "result", result);
    }
}
