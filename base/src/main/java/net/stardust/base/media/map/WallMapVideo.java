package net.stardust.base.media.map;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import net.stardust.base.media.ImageAlignStrategy;
import net.stardust.base.media.ImageAlignStrategy.ImageAlignStrategyEnum;
import net.stardust.base.media.VideoFramer;
import net.stardust.base.utils.ranges.Ranges;

/**
 * Class made to be a {@link VideoFramer} of a video displayed
 * in a screen, usually a wall of maps inside frames in blocks.
 * This class is thread-safe, as specified in {@link VideoFramer}
 * documentation. For a video in only 1 map, {@link MapVideo}
 * is recommended.
 * 
 * @see VideoFramer
 * @see MapVideo
 * 
 * @author Sergio Luis
 */
public class WallMapVideo extends VideoFramer<MapImage[][]> {

    private volatile ImageAlignStrategy strategy;

    private int wallWidth, wallHeight; // Dimensions of the wall in map unities
    private int screenWidth, screenHeight; // Dimensions of the entire screen in pixels

    /**
     * Constructs a {@link WallMapVideo} with the passed {@link InputStream}.
     * This initializes with align strategy {@link ImageAlignStrategyEnum#CENTER}.
     * The wallWidth is the amount of maps per row in the screen.
     * The wallHeight is the amount of maps per column in the screen.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @see ImageAlignStrategyEnum#CENTER
     * @see VideoFramer#VideoFramer(InputStream)
     * @param stream     the stream to read the video data.
     * @param wallWidth  the width of the screen in maps.
     * @param wallHeight the height of the screen in maps.
     * @throws NullPointerException     if stream is null.
     * @throws IllegalArgumentException if wallWidth or wallHeight are 0 or
     *                                  negative.
     */
    public WallMapVideo(InputStream stream, int wallWidth, int wallHeight) {
        super(stream);
        setup(wallWidth, wallHeight);
    }

    /**
     * Constructs a {@link WallMapVideo} with the passed {@link File}.
     * This initializes with align strategy {@link ImageAlignStrategyEnum#CENTER}.
     * The wallWidth is the amount of maps per row in the screen.
     * The wallHeight is the amount of maps per column in the screen.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @see ImageAlignStrategyEnum#CENTER
     * @see VideoFramer#VideoFramer(File)
     * @param file       the file from where read video data.
     * @param wallWidth  the width of the screen in maps.
     * @param wallHeight the height of the screen in maps.
     * @throws NullPointerException     if file is null
     * @throws IllegalArgumentException if wallWidth or wallHeight are 0 or
     *                                  negative.
     * @throws FileNotFoundException    if the file does not exist, is a directory
     *                                  rather than a regular file, or for some
     *                                  other
     *                                  reason cannot be opened for reading.
     */
    public WallMapVideo(File file, int wallWidth, int wallHeight) throws FileNotFoundException {
        super(file);
        setup(wallWidth, wallHeight);
    }

    private void setup(int wallWidth, int wallHeight) {
        int mapSize = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;

        this.wallWidth = Ranges.greater(wallWidth, 0, "wallWidth");
        this.wallHeight = Ranges.greater(wallHeight, 0, "wallHeight");

        screenWidth = wallWidth * mapSize;
        screenHeight = wallHeight * mapSize;

        strategy = ImageAlignStrategyEnum.CENTER.getStrategy();
    }

    @Override
    protected MapImage[][] convertFrame(Java2DFrameConverter converter, Frame frame) {
        BufferedImage image = converter.convert(frame);

        int width = image.getWidth();
        int height = image.getHeight();

        if (width > screenWidth || height > screenHeight) {
            double ratio = (double) width / height;

            if (width > height) {
                width = screenWidth;
                height = (int) (width / ratio);
            } else if (height > width) {
                height = screenHeight;
                width = (int) (height * ratio);
            } else {
                int squareSize;

                if (screenWidth > screenHeight) {
                    squareSize = screenWidth;
                } else {
                    squareSize = screenHeight;
                }

                width = height = squareSize;
            }

            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = resized.createGraphics();
            graphics.drawImage(scaled, 0, 0, null);
            graphics.dispose();

            image = resized;
        }

        int mapSize = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;

        Point corner = strategy.getCorner(width, height, screenWidth, screenHeight);
        MapImage[][] convertedFrame = new MapImage[wallWidth][wallHeight];

        for (int i = 0; i < wallWidth; i++) {
            for (int j = 0; j < wallHeight; j++) {
                convertedFrame[i][j] = new MapImage();
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int x = corner.x + i;
                int y = corner.y + j;

                int arrayX = x / mapSize;
                int arrayY = y / mapSize;

                int mapX = x % mapSize;
                int mapY = y % mapSize;

                int pixel = image.getRGB(i, j);

                convertedFrame[arrayX][arrayY].setPixelColor(mapX, mapY, pixel);
            }
        }

        return convertedFrame;
    }

    /**
     * Returns the {@link ImageAlignStrategy} object being
     * used during render of frames.
     * 
     * @see ImageAlignStrategy
     * @return the align strategy of this map image.
     */
    public ImageAlignStrategy getAlignStrategy() {
        return strategy;
    }

    /**
     * Sets the {@link ImageAlignStrategy} object for being
     * used during render of frames.
     * 
     * @see ImageAlignStrategy
     * @param strategy the new align strategy for this map image.
     * @throws NullPointerException of strategy is null.
     */
    public void setAlignStrategy(ImageAlignStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    /**
     * Sets the {@link ImageAlignStrategy} object using a
     * {@link ImageAlignStrategyEnum}.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @param strategy the strategy enum instance to supply the original align
     *                 strategy for this map image.
     * @throws NullPointerException if strategy is null.
     */
    public void setAlignStrategy(ImageAlignStrategyEnum strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy").getStrategy();
    }

    /**
     * Returns the width of the screen in maps.
     * This is the number of maps per row.
     * 
     * @return the width in maps.
     */
    public int getWallWidth() {
        return wallWidth;
    }

    /**
     * Returns the height of the screen in maps.
     * This is the number of maps per column.
     * 
     * @return the height in maps.
     */
    public int getWallHeight() {
        return wallHeight;
    }

    /**
     * Returns the width of the screen in pixels.
     * This is the sum of all pixels per row in a map
     * of all maps in a row.
     * 
     * @return the width in pixels.
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Returns the height of the screen in pixels.
     * This is the sum of all pixels per column in a map
     * of all maps in a column.
     * 
     * @return the height in pixels.
     */
    public int getScreenHeight() {
        return screenHeight;
    }

}
