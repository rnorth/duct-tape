package org.rnorth.ducttape.circuitbreakers;

/**
 * @author richardnorth
 */
@SuppressWarnings("ALL")
public class ExampleService {
    private long getDelay = 0;
    private int invocationCount = 0;

    public ExampleService() {

    }

    public ExampleService(long getDelay) {
        this.getDelay = getDelay;
    }

    public void sendMessage(Object message) {

    }

    public String getValue(String key) {
        try {
            Thread.sleep(getDelay);
        } catch (InterruptedException ignored) {
        }

        invocationCount++;
        return "value";
    }

    public int getInvocationCount() {
        return invocationCount;
    }
}
