package net.stardust.base.utils.ranges;

/**
 * Utility class for validation of numbers that must
 * be between some range.
 * 
 * @author Sergio Luis
 */
public final class Ranges {

    private Ranges() {
    }

    /**
     * Defines that an int value must be smaller than another; if it
     * is equals or bigger, an {@link IllegalArgumentException} is thrown.
     * 
     * @param value     the value to check
     * @param bigNumber the number limit that the value must be smaller
     * @param varName   the name of the value variable to use in the
     *                  {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed smaller
     * @throws IllegalArgumentException if the value is equal or bigger
     *                                  than the number limit
     */
    public static int smaller(int value, int bigNumber, String varName) {
        if (value >= bigNumber) {
            throw new IllegalArgumentException(varName + " must be smaller than " + bigNumber);
        }
        return value;
    }

    /**
     * Defines that an int value must be smaller or at least equal to another;
     * if it is bigger, an {@link IllegalArgumentException} is thrown.
     * 
     * @param value     the value to check
     * @param bigNumber the number limit that the value must be smaller or equal
     * @param varName   the name of the value variable to use in the
     *                  {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed smaller or equal
     * @throws IllegalArgumentException if the value is bigger than the number limit
     */
    public static int smallerOrEqual(int value, int bigNumber, String varName) {
        if (value > bigNumber) {
            throw new IllegalArgumentException(varName + " must be smaller or equal to " + bigNumber);
        }
        return value;
    }

    /**
     * Defines that an int value must be greater than another; if it is smaller
     * or equal, an {@link IllegalArgumentException} is thrown.
     * 
     * @param value       the value to check
     * @param smallNumber the number limit that the value must be greater
     * @param varName     the name of the value variable to use in the
     *                    {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed greater
     * @throws IllegalArgumentException if the value is smaller or equal to the
     *                                  number limit
     */
    public static int greater(int value, int smallNumber, String varName) {
        if (value <= smallNumber) {
            throw new IllegalArgumentException(varName + " must be greater than " + smallNumber);
        }
        return value;
    }

    /**
     * Defines that an int value must be greater or at least equal to another;
     * if it is smaller, an {@link IllegalArgumentException} is thrown.
     * 
     * @param value       the value to check
     * @param smallNumber the number limit that the value must be greater or equal
     * @param varName     the name of the value variable to use in the
     *                    {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed greater or equal
     * @throws IllegalArgumentException if the value is smaller than the number
     *                                  limit
     */
    public static int greaterOrEqual(int value, int smallNumber, String varName) {
        if (value < smallNumber) {
            throw new IllegalArgumentException(varName + " must be greater or equal to " + smallNumber);
        }
        return value;
    }

    /**
     * Defines a range BI (Bottom Inclusive) and TI (Top Inclusive). If the value
     * is <b>greater or equal</b> to the bottom limit and <b>smaller or equal</b> to
     * the top limit,
     * it is considered valid, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param value   the value to check
     * @param bottom  the bottom limit that the value must be greater or equal
     * @param top     the top limit that the value must be smaller or equal
     * @param varName the name of the value variable to use in the
     *                {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed in the range defined
     * @throws IllegalArgumentException if the value is out of range
     */
    public static int rangeBITI(int value, int bottom, int top, String varName) {
        if (value < bottom || value > top) {
            throw new IllegalArgumentException(
                    varName + " must be in range [" + bottom + ", " + top + "] (inclusive, inclusive)");
        }
        return value;
    }

    /**
     * Defines a range BE (Bottom Exclusive) and TI (Top Inclusive). If the value
     * is <b>greater</b> than the bottom limit and <b>smaller or equal</b> to the
     * top limit,
     * it is considered valid, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param value   the value to check
     * @param bottom  the bottom limit that the value must be greater
     * @param top     the top limit that the value must be smaller or equal
     * @param varName the name of the value variable to use in the
     *                {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed in the range defined
     * @throws IllegalArgumentException if the value is out of range
     */
    public static int rangeBETI(int value, int bottom, int top, String varName) {
        if (value <= bottom || value > top) {
            throw new IllegalArgumentException(
                    varName + " must be in range (" + bottom + ", " + top + "] (exclusive, inclusive)");
        }
        return value;
    }

    /**
     * Defines a range BI (Bottom Inclusive) and TE (Top Exclusive). If the value
     * is <b>greater or equal</b> to the bottom limit and <b>smaller</b> than the
     * top limit,
     * it is considered valid, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param value   the value to check
     * @param bottom  the bottom limit that the value must be greater or equal
     * @param top     the top limit that the value must be smaller
     * @param varName the name of the value variable to use in the
     *                {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed in the range defined
     * @throws IllegalArgumentException if the value if out of range
     */
    public static int rangeBITE(int value, int bottom, int top, String varName) {
        if (value < bottom || value >= top) {
            throw new IllegalArgumentException(
                    varName + " must be in range [" + bottom + ", " + top + ") (inclusive, exclusive)");
        }
        return value;
    }

    /**
     * Defines a range BE (Bottom Exclusive) and TE (Top Exclusive). If the value
     * is <b>greater</b> than the bottom limit and <b>smaller</b> than the top
     * limit,
     * it is considered valid, otherwise an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param value   the value to check
     * @param bottom  the bottom limit that the value must be greater
     * @param top     the top limit that the value must be smaller
     * @param varName the name of the value variable to use in the
     *                {@link IllegalArgumentException} message if thrown
     * @return the value if it's indeed in the range defined
     * @throws IllegalArgumentException if the value is out of range
     */
    public static int rangeBETE(int value, int bottom, int top, String varName) {
        if (value <= bottom || value >= top) {
            throw new IllegalArgumentException(
                    varName + " must be in range (" + bottom + ", " + top + ") (exclusive, exclusive)");
        }
        return value;
    }

    /**
     * Checks if a value is equal to another. If it is different, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param value   the value to check
     * @param mirror  the value that the other one must be equal
     * @param varName the name of the value variable to use in the
     *                {@link IllegalArgumentException} message if thrown
     * @return the value if it is equal to the mirror
     * @throws IllegalArgumentException if the value is different from the mirror
     */
    public static int equals(int value, int mirror, String varName) {
        if (value != mirror) {
            throw new IllegalArgumentException(varName + " must be exactly " + mirror);
        }
        return value;
    }

    /**
     * Checks if a value is different from another. If it is equal, an
     * {@link IllegalArgumentException} is thrown.
     * 
     * @param value     the value to check
     * @param nonMirror the value that the other one must be different
     * @param varName   the name of the value variable to use in the
     *                  {@link IllegalArgumentException} message if thrown
     * @return the value if it is different to the non mirror
     * @throws IllegalArgumentException if the value is equal to the non mirror
     */
    public static int different(int value, int nonMirror, String varName) {
        if (value == nonMirror) {
            throw new IllegalArgumentException(varName + " must be different from " + nonMirror);
        }
        return value;
    }

}
