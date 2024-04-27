package net.stardust.base.utils.ranges;

import java.util.Objects;

public class LongInclusiveRange implements Range<Long> {

    private long smallValue, bigValue;

    public LongInclusiveRange(long smallValue, long bigValue) {
        if(smallValue > bigValue) {
            throw new IllegalArgumentException("smallValue must be less or equal to bigValue");
        }
        this.smallValue = smallValue;
        this.bigValue = bigValue;
    }

    @Override
    public boolean isInRange(Long value) {
        return smallValue <= value && value <= bigValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if(obj instanceof LongInclusiveRange range) {
            return smallValue == range.smallValue && bigValue == range.bigValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(smallValue, bigValue);
    }

}
