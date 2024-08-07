package net.stardust.base.media;

public interface FrameTimestampStrategy {
    
    long getNextTimestamp(long currentTimestamp);

    public enum FrameTimestampStrategyEnum {

        NORMAL(currentTimestamp -> -1),
        MINECRAFT(currentTimestamp -> currentTimestamp == 0 ? 0 : (currentTimestamp / 50000 + 1) * 50000);

        private FrameTimestampStrategy strategy;

        FrameTimestampStrategyEnum(FrameTimestampStrategy strategy) {
            this.strategy = strategy;
        }

        public FrameTimestampStrategy getStrategy() {
            return strategy;
        }

    }

}
