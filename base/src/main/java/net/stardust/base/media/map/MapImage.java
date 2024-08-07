package net.stardust.base.media.map;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import lombok.experimental.StandardException;
import net.stardust.base.media.ImageAlignStrategy;
import net.stardust.base.media.ImageAlignStrategy.StrategyEnum;
import net.stardust.base.utils.ranges.Ranges;

/**
 * A map renderer to draw {@link BufferedImage}s to a
 * Minecraft map with various utility methods for image manipulation.
 * This class is not thread-safe.
 * 
 * @see BufferedImage
 * @see MapRenderer
 * 
 * @author Sergio Luis
 */
public class MapImage extends MapRenderer implements Serializable, Cloneable {

    /**
     * The default square length of a Minecraft map.
     */
    public static final int MINECRAFT_DEFAULT_MAP_SIZE = 128;

    private transient BufferedImage image;
    private ImageAlignStrategy strategy;

    /**
     * Creates a {@link MapImage} with {@link StrategyEnum#CENTER} as
     * default alignment strategy and an empty non null {@link BufferedImage}.
     * 
     * @see BufferedImage
     * @see BufferedImage#BufferedImage(int, int, int)
     * @see ImageAlignStrategy
     * @see StrategyEnum
     */
    public MapImage() {
        image = new BufferedImage(MINECRAFT_DEFAULT_MAP_SIZE, MINECRAFT_DEFAULT_MAP_SIZE, BufferedImage.TYPE_INT_ARGB);
        setAlignStrategy(StrategyEnum.CENTER);
    }

    /**
     * Creates a {@link MapImage} with the already created {@link BufferedImage},
     * changing its type to {@link BufferedImage#TYPE_INT_ARGB} if it is not already
     * that. This uses {@link StrategyEnum#CENTER} as default alignment strategy.
     * 
     * @see MapImage
     * @see BufferedImage
     * @see BufferedImage#TYPE_INT_ARGB
     * @see ImageAlignStrategy
     * @see StrategyEnum
     * @param image the already created buffered image
     * @throws NullPointerException if image is null
     */
    public MapImage(BufferedImage image) {
        setImage(image);
        setAlignStrategy(StrategyEnum.CENTER);
    }

    /**
     * Creates a {@link MapImage} reading data from an {@link InputStream} and
     * creating the internal {@link BufferedImage} object from it. This constructor
     * does not close the stream, it is your responsability to take care of the
     * closing process. The type will be changed to
     * {@link BufferedImage#TYPE_INT_ARGB} if it is not already that. This uses
     * {@link StrategyEnum#CENTER} as default alignment strategy.
     * 
     * @see MapImage
     * @see BufferedImage
     * @see InputStream
     * @see ImageInputStream
     * @see ImageIO
     * @see ImageIO#read(ImageInputStream)
     * @see BufferedImage#TYPE_INT_ARGB
     * @see ImageAlignStrategy
     * @see StrategyEnum
     * @param stream the stream from where read image data.
     * @throws NullPointerException  if stream is null.
     * @throws InvalidImageException if {@link ImageIO#read(InputStream)} returns
     *                               null.
     * @throws IOException           if an error occurs during reading or when
     *                               {@link ImageIO#read(InputStream)} is not able
     *                               to create required {@link ImageInputStream}.
     */
    public MapImage(InputStream stream) throws IOException {
        try {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new InvalidImageException("Could not create an image from the passed stream");
            }
            checkImageType(image);
            setAlignStrategy(StrategyEnum.CENTER);
        } catch (IllegalArgumentException e) {
            NullPointerException exception = new NullPointerException();
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Creates a {@link MapImage} reading bytes from a {@link File}. The type will
     * be changed to {@link BufferedImage#TYPE_INT_ARGB} if it is not already that.
     * 
     * @implNote This constructor just calls {@link MapImage#MapImage(InputStream)}
     *           passing
     * 
     *           <pre>
     *           <code>
     *           new BufferedImage(new FileInputStream(Objects.requireNonNull(file, "file")))
     *           </code>
     *           </pre>
     * 
     *           to it.
     * 
     * @see MapImage
     * @see File
     * @see BufferedInputStream
     * @see FileInputStream
     * @see MapImage#MapImage(InputStream)
     * @see BufferedImage#TYPE_INT_ARGB
     * @param file the file from where read image data.
     * @throws NullPointerException  if file is null.
     * @throws InvalidImageException if {@link ImageIO#read(InputStream)} returns
     *                               null.
     * @throws FileNotFoundException if the file does not exist, is a directory
     *                               rather than a regular file, or for some other
     *                               reason cannot be opened for reading.
     * @throws IOException           if an error occurs during reading or when
     *                               {@link ImageIO#read(InputStream)} is not able
     *                               to create required {@link ImageInputStream}.
     */
    public MapImage(File file) throws FileNotFoundException, IOException {
        this(new BufferedInputStream(new FileInputStream(Objects.requireNonNull(file, "file"))));
    }

    /**
     * <p>
     * Resizes the internal {@link BufferedImage} to the new width and height
     * provided as parameters. This do not preserve the ratio of the original
     * width and height, that is, it does not cut the image for smaller values
     * neighter adds transparent pixels for greater values. This changes the
     * old ratio to the new with width and height, and for that reason, for
     * some values, the quality can be affected. If the width and the height
     * are already the same as the image, this method does nothing.
     * </p>
     * 
     * <p>
     * Also, this method do not changes the original image sizes, it creates
     * a whole new {@link BufferedImage} instance and changes the reference, so
     * if you want to use the old {@link BufferedImage}, use {@link #getImage()}.
     * </p>
     * 
     * @see BufferedImage
     * @see BufferedImage#getType()
     * @see #getImage()
     * @see #resizeToMinecraft()
     * @see #resizeToMinecraftPreservingRatio()
     * @param width  the new width of the image
     * @param height the new height of the image
     * @throws IllegalArgumentException if width or height are 0 or negative.
     */
    public void resize(int width, int height) {
        if (width == getWidth() && height == getHeight()) {
            return;
        }

        Ranges.greater(width, 0, "width");
        Ranges.greater(height, 0, "height");

        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();

        this.image = resized;
    }

    /**
     * Works the same way as {@link #resize(int, int)}, but specifying
     * the default value {@link #MINECRAFT_DEFAULT_MAP_SIZE} as width
     * and height, making a square. This turns the image suitable to be
     * drawn in a map without being clipped.
     * 
     * @see #resize(int, int)
     * @see #resizeToMinecraftPreservingRatio()
     * @see #MINECRAFT_DEFAULT_MAP_SIZE
     */
    public void resizeToMinecraft() {
        resize(MINECRAFT_DEFAULT_MAP_SIZE, MINECRAFT_DEFAULT_MAP_SIZE);
    }

    /**
     * Resizes the image to fit in a Minecraft map preserving the
     * ratio between width and height.
     * 
     * @see #resize(int, int)
     * @see #resizeToMinecraft()
     */
    public void resizeToMinecraftPreservingRatio() {
        int width = getWidth();
        int height = getHeight();

        double ratio = (double) width / height;

        if (width > height) {
            width = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;
            height = (int) (width / ratio);
        } else if (height > width) {
            height = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;
            width = (int) (height * ratio);
        } else {
            width = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;
            height = MapImage.MINECRAFT_DEFAULT_MAP_SIZE;
        }

        resize(width, height);
    }

    /**
     * Renders the image to the given map. The image will be clipped
     * if necessary. To avoid that, use {@link #resize(int, int)}. Also,
     * the image will positioned in the map using the coordinates supplied by the
     * {@link ImageAlignStrategy} to the top-left corner. If the the width of the
     * image is bigger than {@link #MINECRAFT_DEFAULT_MAP_SIZE}, the x
     * coordinate of the top-left corner of the image in the map will be 0.
     * The same logic applies to height.
     * 
     * @see ImageAlignStrategy
     * @see #resize(int, int)
     * @see MapCanvas#drawImage(int, int, Image)
     * @see #MINECRAFT_DEFAULT_MAP_SIZE
     */
    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        Point corner = strategy.getCorner(
                getWidth(),
                getHeight(),
                MINECRAFT_DEFAULT_MAP_SIZE,
                MINECRAFT_DEFAULT_MAP_SIZE);

        canvas.drawImage(corner.x, corner.y, image);
    }

    /**
     * Returns the {@link BufferedImage} of this {@link MapImage}. It is never null.
     * 
     * @see BufferedImage
     * @return the image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Sets the internal reference of the {@link BufferedImage}. This
     * method does not redraw the image to the already drawn maps. Also,
     * this method changes the image type to {@link BufferedImage#TYPE_INT_ARGB}
     * if it is not already that.
     * 
     * @see BufferedImage#TYPE_INT_ARGB
     * @param image the new image reference.
     * @throws NullPointerException if image is null.
     */
    public void setImage(BufferedImage image) {
        checkImageType(Objects.requireNonNull(image, "image"));
    }

    /**
     * Returns the width of the image.
     * 
     * @return the width of the image.
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * Returns the height of the image.
     * 
     * @return the height of the image.
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * Returns the type of the image.
     * 
     * @see BufferedImage#getType()
     * @return the type of the image.
     */
    public int getType() {
        return image.getType();
    }

    /**
     * Returns the pixel color in RGB.
     * 
     * @see BufferedImage#getRGB(int, int)
     * @param x the x coordinate of the pixel.
     * @param y the y coordinate of the pixel.
     * @return the RGB color of the pixel in int format.
     * @throws IllegalArgumentException if x is negative or greater or equal to the
     *                                  width of the image or if y is negative or
     *                                  greater or equal to the height of the image.
     */
    public int getPixelColor(int x, int y) {
        Ranges.rangeBITE(x, 0, image.getWidth(), "x");
        Ranges.rangeBITE(y, 0, image.getHeight(), "y");

        return image.getRGB(x, y);
    }

    /**
     * Sets the RGB color of a pixel of the image.
     * 
     * @see BufferedImage#setRGB(int, int, int)
     * @param x   the x coordinate of the pixel.
     * @param y   the y coordinate of the pixel.
     * @param rgb the RGB color of the pixel in int format.
     * @throws IllegalArgumentException if x is negative or greater or equal to the
     *                                  width of the image of if y is negative of
     *                                  greater or equal to the height of the image.
     */
    public void setPixelColor(int x, int y, int rgb) {
        Ranges.rangeBITE(x, 0, image.getWidth(), "x");
        Ranges.rangeBITE(y, 0, image.getHeight(), "y");

        image.setRGB(x, y, rgb);
    }

    /**
     * Returns a matrix of the pixels of the image. The matrix
     * has the same dimensions of the image and its values are
     * the RGB colors in integer formats of the pixels in each
     * coordinate.
     * 
     * @return a matrix of the pixels of the image.
     */
    public int[][] getPixels() {
        int width = getWidth();
        int height = getHeight();

        int[][] pixels = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        }

        return pixels;
    }

    /**
     * Writes the image to a file, overritting any present
     * data.
     * 
     * @see ImageIO#write(RenderedImage, String, File)
     * @see #writeImage(OutputStream, String)
     * @param file   the file.
     * @param format the format (for example, "png").
     * @return true if the image was successful written to the file, false otherwise
     *         (no appropriate writer is found).
     * @throws NullPointerException if any parameter is null.
     * @throws IOException          if an error occurs during writing or when
     *                              {@link ImageIO#write(RenderedImage, String, File)}
     *                              is not able to create required
     *                              {@link ImageOutputStream}.
     */
    public boolean writeImage(File file, String format) throws IOException {
        return ImageIO.write(image, Objects.requireNonNull(format, "format"), Objects.requireNonNull(file, "file"));
    }

    /**
     * Writes the image to an {@link OutputStream}. This method does not
     * wraps the stream in a {@link BufferedOutputStream} neither closes it, it
     * is your responsability to take care of the closing process.
     * 
     * @see ImageIO#write(RenderedImage, String, OutputStream)
     * @see #writeImage(File, String)
     * @param stream the output stream where write the image.
     * @param format the format (for example, "png").
     * @return true if the image was successful written to the stream, false
     *         otherwise (no appropriate writer is found).
     * @throws NullPointerException if any parameter is null.
     * @throws IOException          if an error occurs during writting or when
     *                              {@link ImageIO#write(RenderedImage, String, OutputStream)}
     *                              is not able to create required
     *                              {@link ImageOutputStream}.
     */
    public boolean writeImage(OutputStream stream, String format) throws IOException {
        return ImageIO.write(image, Objects.requireNonNull(format, "format"), Objects.requireNonNull(stream, "stream"));
    }

    /**
     * Returns the {@link ImageAlignStrategy} object being
     * used during render.
     * 
     * @see ImageAlignStrategy
     * @return the align strategy of this map image.
     */
    public ImageAlignStrategy getAlignStrategy() {
        return strategy;
    }

    /**
     * Sets the {@link ImageAlignStrategy} object for being
     * used during render.
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
     * {@link StrategyEnum}.
     * 
     * @see ImageAlignStrategy
     * @see StrategyEnum
     * @param strategy the strategy enum instance to supply the original align
     *                 strategy for this map image.
     * @throws NullPointerException if strategy is null.
     */
    public void setAlignStrategy(StrategyEnum strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy").getStrategy();
    }

    /**
     * Creates a deep copy of this {@link MapImage}.
     * 
     * @return a deep copy of this object.
     */
    @Override
    public MapImage clone() {
        return new MapImage(createNewImage(getWidth(), getHeight(), getPixels()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof MapImage image) {
            if (getWidth() != image.getWidth() || getHeight() != image.getHeight()) {
                return false;
            }
            return Arrays.deepEquals(getPixels(), image.getPixels());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(getPixels());
    }

    @Serial
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();

        stream.writeInt(getWidth());
        stream.writeInt(getHeight());
        stream.writeObject(getPixels());
    }

    @Serial
    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        int width = stream.readInt();
        int height = stream.readInt();
        int[][] pixels = (int[][]) stream.readObject();

        this.image = createNewImage(width, height, pixels);
    }

    private static BufferedImage createNewImage(int width, int height, int[][] pixels) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                image.setRGB(i, j, pixels[i][j]);
            }
        }

        return image;
    }

    /**
     * Sets the image changing its type to {@link BufferedImage#TYPE_INT_ARGB}
     * if it is not already that.
     * 
     * @see BufferedImage#TYPE_INT_ARGB
     * @param image the image to create copy changing type
     * @throws NullPointerException if image is null
     */
    private void checkImageType(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            this.image = image;
            return;
        }

        this.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = this.image.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
    }

    /**
     * Exception thrown when {@link ImageIO#read(InputStream)} returns null
     * in {@link MapImage#MapImage(InputStream)}.
     * 
     * @see MapImage
     * @see MapImage#MapImage(InputStream)
     * @see ImageIO
     * @see ImageIO#read(InputStream)
     */
    @StandardException
    public class InvalidImageException extends RuntimeException {
    }

}
