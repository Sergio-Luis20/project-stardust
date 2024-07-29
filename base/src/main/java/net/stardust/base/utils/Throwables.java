package net.stardust.base.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import br.sergio.comlib.Communication;
import br.sergio.comlib.Request;

public final class Throwables {
    
    private Throwables() {
    }
    
    public static <T extends Throwable> void writeToFile(File file, T t) {
        try (PrintStream stream = new PrintStream(file)) {
            t.printStackTrace(stream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Throwable> T send(T t) {
        return send(null, t);
    }

    public static <T extends Throwable> T send(String sender, T t) {
        try {
            Communication.send(Request.noMethodRequest(sender, "throwables", t));
        } catch(Exception e) {
            t.addSuppressed(e);
        }
        return t;
    }

    public static void sendAndThrow(Throwable t) {
        sendAndThrow(t, null);
    }

    public static void sendAndThrow(Throwable t, Runnable onFinally) {
        sendAndThrow(null, t, onFinally);
    }

    public static void sendAndThrow(String sender, Throwable t) {
        sendAndThrow(sender, t, null);
    }

    public static void sendAndThrow(String sender, Throwable t, Runnable onFinally) {
        if(t instanceof RuntimeException) {
            throw (RuntimeException) send(sender, t);
        }
        if(t instanceof Error) {
            throw (Error) send(sender, t);
        }
        try {
            throw new RuntimeException(send(sender, t));
        } finally {
            if(onFinally != null) {
                onFinally.run();
            }
        }

    }

}
