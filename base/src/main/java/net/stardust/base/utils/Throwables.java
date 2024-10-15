package net.stardust.base.utils;

import br.sergio.comlib.Communication;
import br.sergio.comlib.Request;

import java.io.*;

/**
 * Utility class to send throwables to the throwable manager plugin.
 * It is recommended to always send a throwable if it is not expected
 * in the context.
 *
 * @author Sergio Luis
 */
public final class Throwables {

    private Throwables() {
    }

    /**
     * Prints the stack trace of a throwable to an output stream.
     * The stream is closed inside this method.
     *
     * @param stream the output stream to print stack trace.
     * @param t      the throwable.
     * @throws NullPointerException if stream or t is null.
     */
    public static void writeToStream(OutputStream stream, Throwable t) {
        try (PrintStream print = new PrintStream(stream)) {
            t.printStackTrace(print);
        }
    }

    /**
     * Prints the stack trace of a throwable to a file.
     *
     * @param file the file.
     * @param t    the throwable.
     * @throws NullPointerException if file or t is null.
     * @throws RuntimeException     if the file was not found, wrapping a FileNotFoundException
     *                              as cause.
     */
    public static void writeToFile(File file, Throwable t) {
        try (PrintStream stream = new PrintStream(file)) {
            t.printStackTrace(stream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Works as specified in {@link #send(String, Throwable)},
     * but setting null as the sender.
     *
     * @param t   the throwable.
     * @param <T> the type of the throwable
     * @return the throwable.
     */
    public static <T extends Throwable> T send(T t) {
        return send(null, t);
    }

    /**
     * Sends a throwable to the throwable manager plugin. If the plugin
     * is not active, this method will fail silently.
     *
     * @param sender the sender.
     * @param t      the throwable.
     * @param <T>    the type of the throwable.
     * @return the throwable.
     */
    public static <T extends Throwable> T send(String sender, T t) {
        try {
            Communication.send(Request.noMethodRequest(sender, "throwables", t));
        } catch (Exception e) {
            t.addSuppressed(e);
        }
        return t;
    }

    /**
     * Works as specified in {@link #sendAndThrow(String, Throwable, Runnable)},
     * but setting sender and onFinally as null.
     *
     * @param t the throwable.
     */
    public static void sendAndThrow(Throwable t) {
        sendAndThrow(t, null);
    }

    /**
     * Works as specified in {@link #sendAndThrow(String, Throwable, Runnable)},
     * but setting sender as null.
     *
     * @param t         the throwable.
     * @param onFinally a runnable object to be executed ina finally block.
     */
    public static void sendAndThrow(Throwable t, Runnable onFinally) {
        sendAndThrow(null, t, onFinally);
    }

    /**
     * Works as specified in {@link #sendAndThrow(String, Throwable, Runnable)},
     * but setting onFinally as null.
     *
     * @param sender the sender.
     * @param t      the throwable.
     */
    public static void sendAndThrow(String sender, Throwable t) {
        sendAndThrow(sender, t, null);
    }

    /**
     * Sends a throwable as specified in {@link #send(String, Throwable)}, but
     * then throws it after sent. If the throwable is checked, it will be wrapped
     * in a RuntimeException to be thrown.
     *
     * @param sender    the sender.
     * @param t         the throwable.
     * @param onFinally a runnable object to be executed in a finally block.
     */
    public static void sendAndThrow(String sender, Throwable t, Runnable onFinally) {
        try {
            if (t instanceof RuntimeException e) {
                throw send(sender, e);
            } else if (t instanceof Error e) {
                throw send(sender, e);
            } else {
                throw new RuntimeException(send(sender, t));
            }
        } finally {
            if (onFinally != null) {
                onFinally.run();
            }
        }
    }

}
