package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
public class TimeSource {

    public long getTimeMillis() {
        return System.currentTimeMillis();
    }

    public static class DummyTimeSource extends TimeSource {

        private long currentTimeMillis = 0L;

        @Override
        public long getTimeMillis() {
            return currentTimeMillis;
        }

        public void setCurrentTimeMillis(long currentTimeMillis) {
            this.currentTimeMillis = currentTimeMillis;
        }
    }
}
