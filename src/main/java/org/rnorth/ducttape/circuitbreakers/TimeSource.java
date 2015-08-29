package org.rnorth.ducttape.circuitbreakers;

/**
 * @author richardnorth
 */
class TimeSource {

    public long getTimeMillis() {
        return System.currentTimeMillis();
    }

    static class DummyTimeSource extends org.rnorth.ducttape.circuitbreakers.TimeSource {

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
