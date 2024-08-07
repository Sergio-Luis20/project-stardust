package net.stardust.base.media.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import net.stardust.base.media.ImageAlignStrategy;
import net.stardust.base.media.VideoFramer;
import net.stardust.base.media.ImageAlignStrategy.ImageAlignStrategyEnum;

/**
 * {@link VideoFramer} implementation for processing frames
 * in a Minecraft map, even if it is in a player hand or on a wall.
 * See {@link VideoFramer} documentation for more details.
 * 
 * <p>
 * Frames generated by this class are resized to fit in a Minecraft map
 * <b>preserving the ratio</b> between width and height.
 * </p>
 * 
 * @apiNote This class is meant to be used in only one map, eighter in the
 *          player hand or in the wall. If you want to use many maps in a wall
 *          to get more pixels for frames, use {@link WallMapVideo} instead.
 * 
 * @see VideoFramer
 * @see WallMapVideo
 * @see MapImage
 * @see FFmpegFrameGrabber
 * @see InputStream
 * 
 * @author Sergio Luis
 */
public class MapVideo extends VideoFramer<MapImage> {

    private volatile ImageAlignStrategy strategy;

    /**
     * Creates a new {@link MapVideo} instance with the passed
     * stream. This initializes with align strategy {@link ImageAlignStrategyEnum#CENTER}.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @see ImageAlignStrategyEnum#CENTER
     * @see VideoFramer#VideoFramer(InputStream)
     * @param stream the stream to read video data
     * @throws NullPointerException if stream is null
     */
    public MapVideo(InputStream stream) {
        super(stream);
        setAlignStrategy(ImageAlignStrategyEnum.CENTER);
    }

    /**
     * Creates a new {@link MapVideo} instance with the passed
     * file. This initializes with align strategy {@link ImageAlignStrategyEnum#CENTER}.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @see ImageAlignStrategyEnum#CENTER
     * @see VideoFramer#VideoFramer(File)
     * @param file the file from where read the video data
     * @throws NullPointerException  if file is null
     * @throws FileNotFoundException if the file does not exist, is a directory
     *                               rather than a regular file, or for some other
     *                               reason cannot be opened for reading.
     */
    public MapVideo(File file) throws FileNotFoundException {
        super(file);
        setAlignStrategy(ImageAlignStrategyEnum.CENTER);
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
     * {@link ImageAlignStrategyEnum} from {@link MapImage} class.
     * 
     * @see ImageAlignStrategy
     * @see ImageAlignStrategyEnum
     * @param strategy the strategy enum instance to supply the original align
     *                 strategy for this map image.
     * @throws NullPointerException if strategy is null.
     */
    public void setAlignStrategy(ImageAlignStrategyEnum strategy) {
        setAlignStrategy(Objects.requireNonNull(strategy, "strategy").getStrategy());
    }

    @Override
    protected MapImage convertFrame(Java2DFrameConverter converter, Frame frame) {
        MapImage image = new MapImage(converter.getBufferedImage(frame));
        image.setAlignStrategy(getAlignStrategy());
        image.resizeToMinecraftPreservingRatio();
        return image;
    }

}
