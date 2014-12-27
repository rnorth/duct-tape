package org.rnorth.circuitbreakers;

/**
 * @author richardnorth
 */
class TimeSource {

    public long getTimeMillis() {
        return System.currentTimeMillis();
    }

    static class DummyTimeSource extends TimeSource {

        private long currentTimeMillis = 0L;

        public DummyTimeSource() {}

        public DummyTimeSource(long fixedValue) {
            this.currentTimeMillis = fixedValue;
        }

        @Override
        public long getTimeMillis() {
            return currentTimeMillis;
        }

        public void setCurrentTimeMillis(long currentTimeMillis) {
            this.currentTimeMillis = currentTimeMillis;
        }
    }
}
