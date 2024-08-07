package net.stardust.base.media;

import java.awt.Point;
import java.io.Serializable;
import java.util.Objects;

/**
 * Interface to define the strategy of how to position
 * the image in the map during rendering. Note that this
 * interface extends {@link Serializable}. Make sure to
 * follow its contract.
 * 
 * @author Sergio Luis
 */
@FunctionalInterface
public interface ImageAlignStrategy extends Serializable {

    /**
     * Gets the coordinates of the top-left corner of the image. Should never
     * return null or neither a point that is out of the bounds of the screen
     * (width and height less than 0 or higher than screen width and height).
     * 
     * @param width        the width of the image to calculate the top-left corner
     *                     coordinates.
     * @param height       the height of the image to calculate the top-left corner
     *                     coordinates.
     * @param screenWidth  the width of the entire drawable screen.
     * @param screenHeight the height of the entire drawable screen.
     * @return the top-left corner coordinates.
     */
    Point getCorner(int width, int height, int screenWidth, int screenHeight);

    /**
     * An utility class that implements {@link ImageAlignStrategy} and
     * returns always the same coordinates, regardless of the parameters.
     * The constructor does not do any verification, so beware negative
     * parameters if they are not allowed. This class overrides equals
     * and hashCode for using with collections and maps.
     * 
     * @see ImageAlignStrategy
     * @see StrategyEnum
     */
    public class ConstantStrategy implements ImageAlignStrategy {

        private final int x, y;

        /**
         * Constructs a new {@link ConstantStrategy} instance
         * with the parameters passed. This constructor does not
         * do any verification, so beware negative parameters if
         * they are not allowed.
         * 
         * @param x the constant x coordinate of the top-left corner of the image.
         * @param y the constant y coordinate of the top-left corner of the image.
         */
        public ConstantStrategy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Point getCorner(int width, int height, int screenWidth, int screenHeight) {
            return new Point(x, y);
        }

        /**
         * Returns the constant x coordinate of the top-left corner of the image.
         * 
         * @return the constant x coordinate of the top-left corner of the image.
         */
        public int getX() {
            return x;
        }

        /**
         * Returns the constant y coordinate of the top-left corner of the image.
         * 
         * @return the constant y coordinate of the top-left corner of the image.
         */
        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (o == this) {
                return true;
            }
            if (o instanceof ConstantStrategy constantStrategy) {
                return x == constantStrategy.x && y == constantStrategy.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

    }

    /**
     * Utility enum to provide default positions and aligning images
     * supplying a {@link ImageAlignStrategy}.
     * 
     * @see ImageAlignStrategy
     * @see ConstantStrategy
     * 
     * @author Sergio Luis
     */
    public enum ImageAlignStrategyEnum {

        /**
         * Aligns the image to the top left position of the screen.
         */
        TOP_LEFT((width, height, screenWidth, screenHeight) -> new Point(0, 0)),

        /**
         * Aligns the image to the top center position of the screen.
         */
        TOP_CENTER((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, (screenWidth - width) / 2);
            return new Point(x, 0);
        }),

        /**
         * Aligns the image to the top right position of the screen.
         */
        TOP_RIGHT((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, screenWidth - width);
            return new Point(x, 0);
        }),

        /**
         * Aligns the image to the center left position of the screen.
         */
        CENTER_LEFT((width, height, screenWidth, screenHeight) -> {
            int y = Math.max(0, (screenHeight - height) / 2);
            return new Point(0, y);
        }),

        /**
         * Aligns the image to the center position of the screen.
         */
        CENTER((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, (screenWidth - width) / 2);
            int y = Math.max(0, (screenHeight - height) / 2);
            return new Point(x, y);
        }),

        /**
         * Aligns the image to the center right position of the screen.
         */
        CENTER_RIGHT((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, screenWidth - width);
            int y = Math.max(0, (screenHeight - height) / 2);
            return new Point(x, y);
        }),

        /**
         * Aligns the image to the bottom left position of the screen.
         */
        BOTTOM_LEFT((width, height, screenWidth, screenHeight) -> {
            int y = Math.max(0, screenHeight - height);
            return new Point(0, y);
        }),

        /**
         * Aligns the image to the bottom center position of the screen.
         */
        BOTTOM_CENTER((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, (screenWidth - width) / 2);
            int y = Math.max(0, screenHeight - height);
            return new Point(x, y);
        }),

        /**
         * Aligns the image to the bottom right position of the screen.
         */
        BOTTOM_RIGHT((width, height, screenWidth, screenHeight) -> {
            int x = Math.max(0, screenWidth - width);
            int y = Math.max(0, screenHeight - height);
            return new Point(x, y);
        });

        private ImageAlignStrategy strategy;

        ImageAlignStrategyEnum(ImageAlignStrategy strategy) {
            this.strategy = strategy;
        }

        /**
         * Returns the {@link ImageAlignStrategy} implementation of
         * this enum instance.
         * 
         * @return the strategy associated to this enum instance.
         */
        public ImageAlignStrategy getStrategy() {
            return strategy;
        }

    }

}