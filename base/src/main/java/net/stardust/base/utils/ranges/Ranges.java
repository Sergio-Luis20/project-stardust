package net.stardust.base.utils.ranges;

public final class Ranges {

    private Ranges() {}

    public static int smaller(int value, int bigNumber, String varName) {
        if(value >= bigNumber) {
            throw new IllegalArgumentException(varName + " must be smaller than " + bigNumber);
        }
        return value;
    }

    public static int smallerOrEqual(int value, int bigNumber, String varName) {
        if(value > bigNumber) {
            throw new IllegalArgumentException(varName + " must be smaller or equal to " + bigNumber);
        }
        return value;
    }

    public static int greater(int value, int smallNumber, String varName) {
        if(value <= smallNumber) {
            throw new IllegalArgumentException(varName + " must be greater than " + smallNumber);
        }
        return value;
    }

    public static int greaterOrEqual(int value, int smallNumber, String varName) {
        if(value < smallNumber) {
            throw new IllegalArgumentException(varName + " must be greater or equal to " + smallNumber);
        }
        return value;
    }

    public static int rangeInclusive(int value, int bottom, int top, String varName) {
        if(value < bottom || value > top) {
            throw new IllegalArgumentException(varName + " must be in range [" + bottom + ", " + top + "] (inclusive)");
        }
        return value;
    }

    public static int rangeExclusive(int value, int bottom, int top, String varName) {
        if(value < bottom || value >= top) {
            throw new IllegalArgumentException(varName + " must be in range [" + bottom + ", " + top + ") (exclusive)");
        }
        return value;
    }

}
