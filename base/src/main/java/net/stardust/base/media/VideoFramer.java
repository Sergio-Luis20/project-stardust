package net.stardust.base.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * <p>
 * Class that represents everything that can process frames of
 * a video and put them in a queue for use.
 * </p>
 * 
 * <p>
 * This class has a queue of a generic type <T> representing the video frames.
 * Those frames are not continuous, they are separated by 50 ms of timestamp,
 * which is 1 tick in Minecraft. Because of that, the video is made to be
 * reproduced synchronously with Bukkit Main thread.
 * </p>
 * 
 * <p>
 * This class is thread-safe, so you can poll frames from the queue in
 * a thread and process the next frame in another if you want.
 * </p>
 * 
 * <p>
 * Closing an instance closes the {@link InputStream} passed as parameter during
 * construction, the internal {@link FFmpegFrameGrabber} and the internal
 * {@link Java2DFrameConverter}, but do not clears the queue, so any frame
 * inside it before the close call will still be there for use.
 * </p>
 * 
 * @see InputStream
 * @see FFmpegFrameGrabber
 * @see Java2DFrameConverter
 * 
 * @author Sergio Luis
 */
public abstract class VideoFramer<T> implements AutoCloseable {

    /**
     * The time of a Minecraft tick in microseconds.
     */
    public static final long MINECRAFT_TICK_MICROS = 50000;

    private final InputStream stream;
    private volatile long timestamp;
    private volatile boolean started, closed;
    private Queue<T> frames;
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;

    /**
     * Creates a new MapVideo instance from an {@link InputStream}. This method
     * do not wraps the {@link InputStream} in a {@link BufferedInputStream},
     * so if you want, you need to pass the buffered one as parameter.
     * 
     * @see InputStream
     * @see BufferedInputStream
     * @param stream the stream to read the video data
     * @throws NullPointerException if stream is null
     */
    public VideoFramer(InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");

        grabber = new FFmpegFrameGrabber(stream);
        converter = new Java2DFrameConverter();
        frames = new LinkedList<>();
    }

    /**
     * Creates a new {@link VideoFramer} instance from a {@link File} reading
     * its data and creating an {@link InputStream} from it.
     * 
     * @implNote This constructor just calls
     *           {@link VideoFramer#VideoFramer(InputStream)}
     *           passing
     * 
     *           <pre>
     *           <code>
     *           new BufferedInputStream(new FileInputStream(Objects.requireNonNull(file, "file")))
     *           </code>
     *           </pre>
     * 
     *           to it.
     * 
     * @see VideoFramer#VideoFramer(InputStream)
     * @param file the file from where read video data
     * @throws NullPointerException  if file is null
     * @throws FileNotFoundException if the file does not exist, is a directory
     *                               rather than a regular file, or for some other
     *                               reason cannot be opened for reading.
     */
    public VideoFramer(File file) throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(Objects.requireNonNull(file, "file"))));
    }

    /**
     * Starts the internal {@link FFmpegFrameGrabber}. Frames cannot
     * be read before this method have been called. Calling it after
     * being called once has no effect.
     * 
     * @see #hasStarted()
     * @throws FFmpegFrameGrabber.Exception if an exception occurs during
     *                                      start process.
     */
    public synchronized void start() throws FFmpegFrameGrabber.Exception {
        if (!started) {
            grabber.start();
            started = true;
        }
    }

    /**
     * Returns if the internal {@link FFmpegFrameGrabber} has started.
     * 
     * @see #start()
     * @return true if grabber started, false otherwise.
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * Returns the number of frames available for polling in the queue.
     * 
     * @return the number of available frames.
     */
    public int availableFrames() {
        synchronized (frames) {
            return frames.size();
        }
    }

    /**
     * Polls a frame from the queue.
     * 
     * @see Queue#poll()
     * @see #pollAllFrames()
     * @return the next frame or null if the queue is empty.
     */
    public T pollFrame() {
        synchronized (frames) {
            return frames.poll();
        }
    }

    /**
     * Polls all frames to a returning list.
     * 
     * @see #pollFrame()
     * @return a list of all frames in the queue or an empty list if it is empty.
     */
    public List<T> pollAllFrames() {
        synchronized (frames) {
            List<T> polledFrames = new ArrayList<>(frames);
            frames.clear();
            return polledFrames;
        }
    }

    /**
     * Grabs the next frame of the video and puts it in the queue for polling.
     * Make sure the grabber has started before calling this method
     * ({@link #hasStarted()}).
     * Also, read {@link VideoFramer} documentation to know more details about
     * how the frames are processed.
     * 
     * @see VideoFramer
     * @see #hasStarted()
     * @see #isClosed()
     * @see #processAllRemainingFrames()
     * @return true if a new frame was successfully added to the queue, false if
     *         this object was closed or for some other reason it can't read more
     *         frames.
     * @throws FrameGrabber.Exception if an exception occurs during frame grabbing.
     */
    public boolean processNextFrame() throws FrameGrabber.Exception {
        synchronized (stream) {
            if (closed) {
                return false;
            }

            Frame frame = grabFrame(grabber);
            if (frame == null) {
                return false;
            }

            addFrameToQueue(frame);

            return true;
        }
    }

    /**
     * <p>
     * Process all remaining frames in the video and adds them to the queue
     * for polling. Note that depending on the size of the video, this method
     * can increase the memory a lot; use it with care. {@link #processNextFrame()}
     * is preferred for cases when you are not sure about the memory consumption
     * the video can cause.
     * </p>
     * 
     * <p>
     * This method doesn't close the stream after reading the entire video.
     * So don't forget to call {@link #close()}.
     * </p>
     * 
     * @see #processNextFrame()
     * @see #close()
     * @return true if all frames were successfully added to the queue, false
     *         if this object was closed or for some reason it can't read more
     *         frames.
     * @throws FFmpegFrameGrabber.Exception if an exception occurs during frame
     *                                      grabbing.
     */
    public boolean processAllRemainingFrames() throws FFmpegFrameGrabber.Exception {
        synchronized (stream) {
            if (closed) {
                return false;
            }

            for (Frame frame; (frame = grabFrame(grabber)) != null;) {
                addFrameToQueue(frame);
            }

            return true;
        }
    }

    /**
     * Converts and adds the frame to the queue if not null for polling
     * and updates the timestamp of the grabber to next frame.
     * 
     * @param converter the converter
     * @param frame     the frame to be converted
     * @throws FFmpegFrameGrabber.Exception
     */
    private void addFrameToQueue(Frame frame) throws FFmpegFrameGrabber.Exception {
        T converted = convertFrame(converter, frame);
        if (converted == null) {
            return;
        }

        synchronized (frames) {
            frames.add(converted);
            timestamp += MINECRAFT_TICK_MICROS;
            grabber.setTimestamp(timestamp);
        }
    }

    /**
     * Grabbs a frame. The default implementation just calls
     * {@link FFmpegFrameGrabber#grabImage()}, but you can
     * override this method if you want to grab a different
     * kind of frame, maybe incluing audio for example.
     * 
     * @param grabber the frame grabber.
     * @return the grabbed frame.
     * @throws FFmpegFrameGrabber.Exception if an exception occurs during grab.
     */
    protected Frame grabFrame(FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
        return grabber.grabImage();
    }

    /**
     * Converts the raw frame to the genetic type parameter of this class.
     * 
     * @param converter the converter
     * @param frame     the frame to be converted
     * @return the converted frame
     */
    protected abstract T convertFrame(Java2DFrameConverter converter, Frame frame);

    /**
     * Returns the current timestamp of the video in microseconds.
     * Note: this is not the timestamp of the last frame calculated,
     * it is the one that will be used for calculation of the next
     * frame.
     * 
     * @return the current timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Closes the grabber, the converter and the stream and sets the
     * "closed" internal flag to true.
     * 
     * @see AutoCloseable
     * @see AutoCloseable#close()
     * @see #isClosed()
     * @throws Exception if an exception occurs during
     *                   the close process of the grabber or the stream.
     */
    @Override
    public void close() throws Exception {
        synchronized (stream) {
            if (closed) {
                return;
            }
            try (stream) {
                grabber.close();
                converter.close();
            } finally {
                closed = true;
            }
        }
    }

    /**
     * Returns if this object has been closed.
     * 
     * @see #close()
     * @return true if this object has been closed,
     *         false otherwise.
     */
    public boolean isClosed() {
        return closed;
    }

}
