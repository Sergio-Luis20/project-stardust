package net.stardust.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Utility class for serializing and deserializing objects.
 * If the objects are not instances of {@link java.io.Serializable} or
 * {@link java.io.Externalizable} as specified in the documentation of
 * {@link java.io.ObjectInputStream} and {@link java.io.ObjectOutputStream},
 * the same exceptions are thrown.
 * 
 * @author Sergio Luis
 */
public final class Serializer {

    /**
     * Final utility class. Should never be instantiated.
     */
    private Serializer() {}
    
    /**
     * Serializes an object into a byte array and returns that byte array.
     * Careful with memory if the object is too complex to store inside a byte
     * array in the RAM.
     * @param obj the object to be serialized.
     * @return the serialized object as byte array.
     * @throws IOException if an IOException occurs during the serialization.
     */
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serialize(obj, baos);
        return baos.toByteArray();
    }

    /**
     * Serializes an object to the given OutputStream throught an
     * ObjectOutputStream. This wraps the out stream in a BufferedOutputStream.
     * By default, this method closes the stream.
     * @param obj the object to be serialized.
     * @param out the output stream into what the object is going to be serialized.
     * @throws IOException if an IOException occurs inside the ObjectOutputStream during serialization.
     */
    public static void serialize(Object obj, OutputStream out) throws IOException {
        try(BufferedOutputStream buf = new BufferedOutputStream(out); ObjectOutputStream objOut = new ObjectOutputStream(buf)) {
            objOut.writeObject(obj);
        }
    }

    /**
     * Utility method that deserializes and object from a byte array as described in
     * {@link #deserialize(byte[])}, but casting it to the given class. If the object
     * is not an instance of the given class, a {@link java.lang.ClassCastException} is thrown.
     * This is equivalent of doing
     * <pre>
     * return componentType.cast(deserialize(serializedData));
     * </pre>
     * @param <T> the type to cast the deserialized object
     * @param serializedData the byte array that will be read into an object
     * @param componentType the class providing the type to cast the deserialized object
     * @return the deserialized object from the provided byte array cast to the type provided by the class argument
     * @throws NullPointerException if serializedData and/or componentType is null
     * @throws ClassCastException if the provided class do not match the deserialized type
     * @throws IOException if an IOException occurs during deserialization
     * @throws ClassNotFoundException if the deserialized class was not found
     */
    public static <T> T deserialize(byte[] serializedData, Class<T> componentType) throws IOException, ClassNotFoundException {
        return componentType.cast(deserialize(serializedData));
    }

    /**
     * Utility method that deserializes an object from an InputStream as described in
     * {@link #deserialize(InputStream)}, but casting it to the given class. If the object
     * is not an instance of the given class, a {@link java.lang.ClassCastException} is thrown.
     * This is equivalent of doing
     * <pre>
     * return componentType.cast(deserialize(in));
     * </pre>
     * @param <T> the class.
     * @param in the input stream.
     * @param componentType the class to cast the deserialized object.
     * @return the deserialized object as instance of the given class.
     * @throws IOException if an IOException occurs during the deserialization.
     * @throws ClassNotFoundException if the proper class of the deserialized object was not found.
     */
    public static <T> T deserialize(InputStream in, Class<T> componentType) throws IOException, ClassNotFoundException {
        return componentType.cast(deserialize(in));
    }
    
    /**
     * Deserializes an object from a byte array and returns that object.
     * @param serializedData the serialized object as byte array.
     * @return the deserialized object.
     * @throws IOException if an IOException occurs during the deserialization.
     * @throws ClassNotFoundException if the class of the deserialized object was not found.
     */
    public static Object deserialize(byte[] serializedData) throws IOException, ClassNotFoundException {
        return deserialize(new ByteArrayInputStream(serializedData));
    }

    /**
     * Deserializes an object from the given InputStream throught an
     * ObjectInputStream. This wraps the in stream in a BufferedInputStream.
     * By default, this method closes the stream.
     * @param in the input stream from what the object is going to be deserialized.
     * @return the deserialized object.
     * @throws IOException if an IOException occurs inside the ObjectInputStream during deserializaton.
     * @throws ClassNotFoundException if the class of the deserialized object was not found.
     */
    public static Object deserialize(InputStream in) throws IOException, ClassNotFoundException {
        try(BufferedInputStream buf = new BufferedInputStream(in); ObjectInputStream objIn = new ObjectInputStream(buf)) {
            return objIn.readObject();
        }
    }

    /**
     * Creates a buffered object input stream to read data directly.
     * Should be used in a try-with-resources.
     * @param in the input stream to read data
     * @return an open ObjectInputStream to read data directly
     * @throws NullPointerException if in is null
     * @throws IOException if an IOException occurs during stream creation
     */
    public static ObjectInputStream openReader(InputStream in) throws IOException {
        BufferedInputStream buf = new BufferedInputStream(Objects.requireNonNull(in, "in"));
        return new ObjectInputStream(buf);
    }

    /**
     * Creates a buffered object output stream to write data directly.
     * Should be used in a try-with-resources.
     * @param out the output stream to write data
     * @return an open ObjectOutputStream to write data directly.
     * @throws NullPointerException if out is null
     * @throws IOException if an IOException occurs during stream creation
     */
    public static ObjectOutputStream openWriter(OutputStream out) throws IOException {
        BufferedOutputStream buf = new BufferedOutputStream(Objects.requireNonNull(out, "out"));
        return new ObjectOutputStream(buf);
    }

}
