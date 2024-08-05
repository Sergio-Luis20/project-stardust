package net.stardust.base.utils.persistence;

import static net.stardust.base.Stardust.key;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import net.stardust.base.Stardust;
import net.stardust.base.utils.Serializer;
import net.stardust.base.utils.Throwables;

/**
 * <p>
 * Utility class that wraps a {@link PersistentDataHolder} and manages
 * different types of data, being able to serialize and deserializes from its
 * {@link PersistentDataContainer}. For every type, even the primitives,
 * this class saves it as byte arrays using {@link ObjectOutputStream} in
 * conjunction with {@link ByteArrayOutputStream}, so every object to serialize
 * must implement {@link Serializable}. For that reason, in Stardust server,
 * never manage data directly from the {@link PersistentDataContainer} interface
 * to avoid lacks of integrity; always use this class.
 * </p>
 * 
 * This class itself implements {@link PersistentDataHolder}; use this feature
 * with care.
 * 
 * @see PersistentDataHolder
 * @see PersistentDataContainer
 * @see ObjectOutputStream
 * @see ByteArrayOutputStream
 * @see ObjectInputStream
 * @see ByteArrayInputStream
 * @see Serializable
 * @see Serializer
 * 
 * @author Sergio Luis
 */
public class DataManager<T extends PersistentDataHolder> implements PersistentDataHolder, Iterable<NamespacedKey> {

    /**
     * The wrapped {@link PersistentDataHolder}. Do not modify it
     * using reflection.
     */
    private T holder;

    /**
     * Creates a new {@link DataManager} wrapping the {@link PersistentDataHolder}
     * parameter.
     * The parameter must not be null.
     * 
     * @param holder the persistent data holder to wrap
     * @throws NullPointerException if holder is null
     */
    public DataManager(T holder) {
        setHolder(holder);
    }

    /**
     * Returns the wrapped {@link PersistentDataHolder}. It is never null.
     * 
     * @return the wrapped holder
     */
    public T getHolder() {
        return holder;
    }

    /**
     * Sets the new {@link PersistentDataHolder} to where manipulate data.
     * Cannot be null.
     * 
     * @param holder the new holder
     * @throws NullPointerException if holder is null
     */
    public void setHolder(T holder) {
        this.holder = Objects.requireNonNull(holder, "holder");
    }

    /**
     * Returns an {@link Iterator} of the {@link NamespacedKey}s of the
     * wrapped {@link PersistentDataHolder}.
     * 
     * @see Iterator
     * @see NamespacedKey
     * @see PersistentDataHolder
     * @return the keys iterator of the wrapped holder
     */
    @Override
    public Iterator<NamespacedKey> iterator() {
        return getKeys().iterator();
    }

    /**
     * Returns the {@link PersistentDataContainer} of the wrapped
     * {@link PersistenDataHolder}.
     * 
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @returns the container of the wrapped holder
     */
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return holder.getPersistentDataContainer();
    }

    /**
     * Returns a {@link Set} of the {@link NamespacedKey}s presents in the
     * {@link PersistentDataContainer} of the wrapped {@link PersistentDataHolder}.
     * As said in documentation of {@link PersistentDataContainer#getKeys()}, the
     * returned set is independent copy and changes on it will not affect the
     * internal keys.
     * 
     * @see Set
     * @see NamespacedKey
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @see PersistentDataContainer#getKeys()
     * @see #getKeysAsString()
     * @return the keys inside the container of the wrapped holder
     */
    public Set<NamespacedKey> getKeys() {
        return getPersistentDataContainer().getKeys();
    }

    /**
     * Returns the same {@link Set} as described in documentation of
     * {@link #getKeys()},
     * but transforming every {@link NamespacedKey} in its String form.
     * 
     * @see Set
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see #getKeys()
     * @return the keys inside the container of the wrapped holder in its String
     *         format
     */
    public Set<String> getKeysAsString() {
        return getKeys().stream().map(NamespacedKey::asString).collect(Collectors.toSet());
    }

    /**
     * Checks if a {@link NamespacedKey} is associated with any value inside the
     * {@link PersistentDataContainer} of the wrapped {@link PersistentDataHolder}.
     * Check {@link PersistentDataContainer#has(NamespacedKey)} for more
     * implementation
     * details.
     * 
     * @see NamespacedKey
     * @see PersistentDataHolder
     * @see PersistentDatacontainer
     * @see PersistentDataContainer#has(NamespacedKey)
     * @see #hasKey(String)
     * @param key the key to check existence inside the container of the wrapped
     *            holder
     * @return true if the key is present inside the container, false otherwise
     * @throws NullPointerException if key is null
     */
    public boolean hasKey(NamespacedKey key) {
        try {
            return getPersistentDataContainer().has(key);
        } catch (IllegalArgumentException e) {
            NullPointerException exception = new NullPointerException("null key");
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Works the same way as {@link #hasKey(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #hasKey(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return true if the key is present inside the container, false otherwise
     * @throws NullPointerException     if key is null
     * @throws IllegalArgumentException if the passed String is not in the
     *                                  {@link NamespacedKey} syntax or the String
     *                                  is empty or has 256 or more chars
     */
    public boolean hasKey(String key) {
        return hasKey(key(key));
    }

    /**
     * Removes an entry from the {@link PersistentDataContainer} of the wrapped
     * {@link PersistentDataHolder} by its {@link NamespacedKey}. The parameter key
     * cannot be null. If the container has no mapping for that key, this method
     * fails silenlty.
     * 
     * @see NamespacedKey
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @see PersistentDataContainer#remove(NamespacedKey)
     * @see #remove(String)
     * @param key the key whose entry will be removed from container
     * @throws NullPointerException if key is null
     */
    public void remove(NamespacedKey key) {
        try {
            getPersistentDataContainer().remove(key);
        } catch (IllegalArgumentException e) {
            NullPointerException exception = new NullPointerException("null key");
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Works the same way as {@link #remove(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #remove(NamespacedKey)
     * @param key the the {@link NamespacedKey} in its String form whose entry
     *            will be removed from container
     * @throws NullPointerException     if key is null
     * @throws IllegalArgumentException if the passed String is not in the
     *                                  {@link NamespacedKey} syntax or the String
     *                                  is empty or has 256 or more chars
     */
    public void remove(String key) {
        remove(key(key));
    }

    /**
     * Reads a byte as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapByte(String)
     * @param key the key
     * @return the wrapper {@link Byte}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Byte}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Byte readWrapByte(NamespacedKey key) {
        return readObject(key, Byte.class);
    }

    /**
     * Works the same way as {@link #readWrapByte(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapByte(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Byte}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Byte}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Byte readWrapByte(String key) {
        return readWrapByte(key(key));
    }

    /**
     * Reads a byte from the container using the parameter key. If no byte was found
     * for the given key, this returns the byte {@code 0}.
     * 
     * @see #readByte(String)
     * @param key the key
     * @return the byte
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Byte}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public byte readByte(NamespacedKey key) {
        Byte b = readWrapByte(key);
        return b == null ? 0 : b;
    }

    /**
     * Works the same way as {@link #readByte(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readByte(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the byte
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Byte}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public byte readByte(String key) {
        return readByte(key(key));
    }

    /**
     * Reads a short as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapShort(String)
     * @param key the key
     * @return the wrapper {@link Short}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Short}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Short readWrapShort(NamespacedKey key) {
        return readObject(key, Short.class);
    }

    /**
     * Works the same way as {@link #readWrapShort(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapShort(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Short}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Short}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Short readWrapShort(String key) {
        return readWrapShort(key(key));
    }

    /**
     * Reads a short from the container using the parameter key. If no short was
     * found
     * for the given key, this returns the short {@code 0}.
     * 
     * @see #readShort(String)
     * @param key the key
     * @return the short
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Short}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public short readShort(NamespacedKey key) {
        Short s = readWrapShort(key);
        return s == null ? 0 : s;
    }

    /**
     * Works the same way as {@link #readShort(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readShort(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the short
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Short}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public short readShort(String key) {
        return readShort(key(key));
    }

    /**
     * Reads an integer as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapInteger(String)
     * @param key the key
     * @return the wrapper {@link Integer}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Integer}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Integer readWrapInteger(NamespacedKey key) {
        return readObject(key, Integer.class);
    }

    /**
     * Works the same way as {@link #readWrapInteger(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapInteger(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Integer}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Integer}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Integer readWrapInteger(String key) {
        return readWrapInteger(key(key));
    }

    /**
     * Reads an int from the container using the parameter key. If no int was found
     * for the given key, this returns the int {@code 0}.
     * 
     * @see #readInt(String)
     * @param key the key
     * @return the int
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Integer}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public int readInt(NamespacedKey key) {
        Integer i = readWrapInteger(key);
        return i == null ? 0 : i;
    }

    /**
     * Works the same way as {@link #readInt(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readInt(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the int
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Integer}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public int readInt(String key) {
        return readInt(key(key));
    }

    /**
     * Reads a long as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapLong(String)
     * @param key the key
     * @return the wrapper {@link Long}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Long}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Long readWrapLong(NamespacedKey key) {
        return readObject(key, Long.class);
    }

    /**
     * Works the same way as {@link #readWrapLong(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapLong(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Long}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Long}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Long readWrapLong(String key) {
        return readWrapLong(key(key));
    }

    /**
     * Reads a long from the container using the parameter key. If no long was found
     * for the given key, this returns the long {@code 0}.
     * 
     * @see #readLong(String)
     * @param key the key
     * @return the long
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Long}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public long readLong(NamespacedKey key) {
        Long l = readWrapLong(key);
        return l == null ? 0 : l;
    }

    /**
     * Works the same way as {@link #readLong(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readLong(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the long
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Long}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public long readLong(String key) {
        return readLong(key(key));
    }

    /**
     * Reads a float as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapFloat(String)
     * @param key the key
     * @return the wrapper {@link Float}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Float}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Float readWrapFloat(NamespacedKey key) {
        return readObject(key, Float.class);
    }

    /**
     * Works the same way as {@link #readWrapFloat(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapFloat(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Float}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Float}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Float readWrapFloat(String key) {
        return readWrapFloat(key(key));
    }

    /**
     * Reads a float from the container using the parameter key. If no float was
     * found
     * for the given key, this returns the float {@code 0}.
     * 
     * @see #readFloat(String)
     * @param key the key
     * @return the float
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Float}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public float readFloat(NamespacedKey key) {
        Float f = readWrapFloat(key);
        return f == null ? 0 : f;
    }

    /**
     * Works the same way as {@link #readFloat(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readFloat(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the float
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Float}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public float readFloat(String key) {
        return readFloat(key(key));
    }

    /**
     * Reads a double as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapDouble(String)
     * @param key the key
     * @return the wrapper {@link Double}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Double}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Double readWrapDouble(NamespacedKey key) {
        return readObject(key, Double.class);
    }

    /**
     * Works the same way as {@link #readWrapDouble(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapDouble(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Double}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Double}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Double readWrapDouble(String key) {
        return readWrapDouble(key(key));
    }

    /**
     * Reads a double from the container using the parameter key. If no double was
     * found
     * for the given key, this returns the double {@code 0}.
     * 
     * @see #readDouble(String)
     * @param key the key
     * @return the double
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Double}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public double readDouble(NamespacedKey key) {
        Double d = readWrapDouble(key);
        return d == null ? 0 : d;
    }

    /**
     * Works the same way as {@link #readDouble(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readDouble(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the double
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Double}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public double readDouble(String key) {
        return readDouble(key(key));
    }

    /**
     * Reads a boolean as wrapper class from the container using the key parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapBoolean(String)
     * @param key the key
     * @return the wrapper {@link Boolean}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Boolean}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Boolean readWrapBoolean(NamespacedKey key) {
        return readObject(key, Boolean.class);
    }

    /**
     * Works the same way as {@link #readWrapBoolean(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapBoolean(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Boolean}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Boolean}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Boolean readWrapBoolean(String key) {
        return readWrapBoolean(key(key));
    }

    /**
     * Reads a boolean from the container using the parameter key. If no boolean was
     * found
     * for the given key, this returns the boolean {@code false}.
     * 
     * @see #readBoolean(String)
     * @param key the key
     * @return the boolean
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Boolean}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public boolean readBoolean(NamespacedKey key) {
        Boolean b = readWrapBoolean(key);
        return b == null ? false : b;
    }

    /**
     * Works the same way as {@link #readBoolean(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readBoolean(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the boolean
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Boolean}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public boolean readBoolean(String key) {
        return readBoolean(key(key));
    }

    /**
     * Reads a character as wrapper class from the container using the key
     * parameter.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readWrapCharacter(String)
     * @param key the key
     * @return the wrapper {@link Character}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Character}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Character readWrapCharacter(NamespacedKey key) {
        return readObject(key, Character.class);
    }

    /**
     * Works the same way as {@link #readWrapCharacter(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readWrapCharacter(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the wrapper {@link Character}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Character}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Character readWrapCharacter(String key) {
        return readWrapCharacter(key(key));
    }

    /**
     * Reads a char from the container using the parameter key. If no char was found
     * for the given key, this returns the char {@code '\0'} (the null char).
     * 
     * @see #readChar(String)
     * @param key the key
     * @return the char
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link Character}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public char readChar(NamespacedKey key) {
        Character c = readWrapCharacter(key);
        return c == null ? '\0' : c;
    }

    /**
     * Works the same way as {@link #readChar(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readChar(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the char
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link Character}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public char readChar(String key) {
        return readChar(key(key));
    }

    /**
     * Reads a String from the container using the parameter key.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readString(String)
     * @param key the key
     * @return the {@link String}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link String}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public String readString(NamespacedKey key) {
        return readObject(key, String.class);
    }

    /**
     * Works the same way as {@link #readString(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readString(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the {@link String}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link String}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public String readString(String key) {
        return readString(key(key));
    }

    /**
     * Reads an UUID from the container using the parameter key.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readUUID(String)
     * @param key the key
     * @return the {@link UUID}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link UUID}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public UUID readUUID(NamespacedKey key) {
        return readObject(key, UUID.class);
    }

    /**
     * Works the same way as {@link #readUUID(NamespacedKey)}, but using a parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readUUID(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the {@link UUID}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link UUID}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public UUID readUUID(String key) {
        return readUUID(key(key));
    }

    /**
     * Reads a BigInteger from the container using the parameter key.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readBigInteger(String)
     * @param key the key
     * @return the {@link BigInteger}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link BigInteger}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public BigInteger readBigInteger(NamespacedKey key) {
        return readObject(key, BigInteger.class);
    }

    /**
     * Works the same way as {@link #readBigInteger(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readBigInteger(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the {@link BigInteger}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link BigInteger}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public BigInteger readBigInteger(String key) {
        return readBigInteger(key(key));
    }

    /**
     * Reads a BigDecimal from the container using the parameter key.
     * If no mapped value was found for the provided key, this method returns null.
     * 
     * @see #readBigDecimal(String)
     * @param key the key
     * @return the {@link BigDecimal}
     * @throws NullPointerException              if key is null
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance of
     *                                           {@link BigDecimal}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public BigDecimal readBigDecimal(NamespacedKey key) {
        return readObject(key, BigDecimal.class);
    }

    /**
     * Works the same way as {@link #readBigDecimal(NamespacedKey)}, but using a
     * parameter
     * String that represents a {@link NamespacedKey} in its String format with the
     * same syntax as returned by {@link NamespacedKey#asString()}. The passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readBigDecimal(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the {@link BigDecimal}
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           {@link BigDecimal}
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public BigDecimal readBigDecimal(String key) {
        return readBigDecimal(key(key));
    }

    /**
     * Reads the object from the container using the parameter key.
     * If no mapping is found for the passed key inside the container,
     * this method returns null. The key parameter cannot be null.
     * 
     * @param key the key
     * @return the object associated with the parameter key inside the container
     * @throws NullPointerException              if key is null
     * @throws ClassNotFoundException            if the deserialized object class
     *                                           was not found
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Object readObject(NamespacedKey key) {
        Objects.requireNonNull(key, "null key");
        var list = getDefaultDataTypes();
        var container = getPersistentDataContainer();
        Object found = null;
        for (var type : list) {
            try {
                found = container.get(key, type);
                if (found == null) {
                    continue;
                }
                break;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        if (found instanceof byte[] array) {
            try {
                return Serializer.deserialize(array);
            } catch (IOException | ClassNotFoundException e) {
                Throwables.send("PersistentDataManager/readObject", e);
                throw new PersistenceSerializationException("IOException during deserialization", e);
            }
        }
        return found;
    }

    /**
     * Works the same way as {@link #readObject(NamespacedKey)} but using a
     * String parameter that represents a {@link NamespacedKey} in its String format
     * with the same syntax as returned by {@link NamespacedKey#asString()}. The
     * passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readObject(NamespacedKey)
     * @param key the {@link NamespacedKey} in its String form
     * @return the object associated with the parameter key inside the container
     * @throws NullPointerException              if key is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassNotFoundException            if the deserialized object class
     *                                           was not
     *                                           found
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public Object readObject(String key) {
        return readObject(key(key));
    }

    /**
     * Writes an object into the container associating it with the
     * key parameter. If the container already contains a mapped value
     * for that key, it will be overriden. If the passed object is null,
     * any mapped value associated with the key parameter inside the
     * container will be erased. If the wrapped holder is an instance of
     * {@link TileState}, then {@link TileState#update()} will be called
     * after write.
     * 
     * @see TileState
     * @see TileState#update()
     * @param key    the key
     * @param object the object to associate with the key into the container
     * @throws NullPointerException              if key is null
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           serialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public void writeObject(NamespacedKey key, Object object) {
        try {
            T holder = getHolder();
            var container = holder.getPersistentDataContainer();
            if (object == null) {
                remove(key);
            } else {
                try {
                    container.set(key, PersistentDataType.BYTE_ARRAY, Serializer.serialize(object));
                } catch (IllegalArgumentException e) {
                    NullPointerException exception = new NullPointerException("null key");
                    exception.initCause(e);
                    throw exception;
                }
                if (holder instanceof TileState state)
                    state.update();
            }
        } catch (IOException e) {
            Throwables.send("PersistentDataManager/writeObject", e);
            throw new PersistenceSerializationException("IOException during serialization", e);
        }
    }

    /**
     * Works the same way as {@link #writeObject(NamespacedKey, Object)} but using a
     * String parameter that represents a {@link NamespacedKey} in its String format
     * with the same syntax as returned by {@link NamespacedKey#asString()}. The
     * passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #writeObject(NamespacedKey, Object)
     * @param key    the {@link NamespacedKey} in its String form
     * @param object the object to associate with the key into the container
     * @throws NullPointerException              if key is null
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           serialization.
     *                                           The {@link IOException} will be the
     *                                           cause of the
     *                                           thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public void writeObject(String key, Object object) {
        writeObject(key(key), object);
    }

    /**
     * Deserializes an object as specified in {@link #readObject(NamespacedKey)}
     * documentation. The class parameter will be used to cast the deserialized
     * object.
     * 
     * @see #readObject(NamespacedKey)
     * @param <U>           the type to cast
     * @param key           the key for searching for mappings inside the container
     *                      as specified
     *                      in {@link #readObject(NamespacedKey)} documentation
     * @param componentType the class to cast the deserialized object
     * @return the deserialized object as an instance of the class parameter
     * @throws NullPointerException              if any parameter is null
     * @throws ClassNotFoundException            if the deserialized object class
     *                                           was not found
     * @throws ClassCastException                if the deserialized object is not
     *                                           an instance
     *                                           of
     *                                           the class parameter
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public <U> U readObject(NamespacedKey key, Class<U> componentType) {
        return componentType.cast(readObject(key));
    }

    /**
     * Works the same way as {@link #readObject(NamespacedKey, Class)} but using a
     * String parameter that represents a {@link NamespacedKey} in its String format
     * with the same syntax as returned by {@link NamespacedKey#asString()}. The
     * passed
     * String cannot be null. Read {@link NamespacedKey} documentation to know
     * that syntax.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see Stardust#key(String)
     * @see #readObject(NamespacedKey, Class)
     * @param <U>           the type to cast
     * @param key           the {@link NamespacedKey} in its String form
     * @param componentType the class to cast the deserialized object
     * @return the deserialized object as an instance of the class parameter
     * @throws NullPointerException              if any parameter is null
     * @throws IllegalArgumentException          if the passed String is not in the
     *                                           {@link NamespacedKey} syntax or the
     *                                           String
     *                                           is empty or has 256 or more chars
     * @throws ClassNotFoundException            if the deserialized object class
     *                                           was not
     *                                           found
     * @throws ClassCastException                if the deserialized object is not
     *                                           an
     *                                           instance of
     *                                           the class parameter
     * @throws PersistenceSerializationException if an {@link IOException} occurs
     *                                           during
     *                                           deserialization.
     *                                           The {@link IOException} will be the
     *                                           cause of
     *                                           the thrown
     *                                           {@link PersistenceSerializationException}.
     */
    public <U> U readObject(String key, Class<U> componentType) {
        return readObject(key(key), componentType);
    }

    /**
     * Returns an unmodifiable list of all simple constants presentF
     * in {@link PersistentDataType}. A simple constant here means that
     * it includes only the primitive wrapper types, the String type and
     * the byte, integer and long array types.
     * 
     * @see PersistentDataType
     * @return a list of all data types
     */
    public static List<PersistentDataType<?, ?>> getDefaultDataTypes() {
        return Arrays.asList(
                PersistentDataType.BYTE,
                PersistentDataType.SHORT,
                PersistentDataType.INTEGER,
                PersistentDataType.LONG,
                PersistentDataType.FLOAT,
                PersistentDataType.DOUBLE,
                PersistentDataType.BOOLEAN,
                PersistentDataType.STRING,
                PersistentDataType.BYTE_ARRAY,
                PersistentDataType.INTEGER_ARRAY,
                PersistentDataType.LONG_ARRAY);
    }

}
