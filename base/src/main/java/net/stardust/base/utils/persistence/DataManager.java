package net.stardust.base.utils.persistence;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.stardust.base.utils.Serializer;
import net.stardust.base.utils.Throwables;

@Data
@AllArgsConstructor
public class DataManager<T extends PersistentDataHolder> implements PersistentDataHolder, Iterable<NamespacedKey> {

    private T holder;

    @Override
    public Iterator<NamespacedKey> iterator() {
        return getKeys().iterator();
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return getHolder().getPersistentDataContainer();
    }

    public PersistentDataContainer getContainer() {
        return getPersistentDataContainer();
    }

    public Set<NamespacedKey> getKeys() {
        return getContainer().getKeys();
    }

    public Set<String> getKeysAsString() {
        return getKeys().stream().map(NamespacedKey::asString).collect(Collectors.toSet());
    }

    public boolean hasKey(NamespacedKey key) {
        return getContainer().has(key);
    }

    public boolean hasKey(String key) {
        return hasKey(key(key));
    }
    
    public void remove(NamespacedKey key) {
        getContainer().remove(key);
    }

    public void remove(String key) {
        remove(key(key));
    }

    public Byte readWrapByte(NamespacedKey key) {
        return readObject(key, Byte.class);
    }

    public Byte readWrapByte(String key) {
        return readWrapByte(key(key));
    }

    public byte readByte(NamespacedKey key) {
        Byte b = readWrapByte(key);
        return b == null ? 0 : b;
    }
    
    public byte readByte(String key) {
        return readByte(key(key));
    }

    public Short readWrapShort(NamespacedKey key) {
        return readObject(key, Short.class);
    }

    public Short readWrapShort(String key) {
        return readWrapShort(key(key));
    }

    public short readShort(NamespacedKey key) {
        Short s = readWrapShort(key);
        return s == null ? 0 : s;
    }
    
    public short readShort(String key) {
        return readShort(key(key));
    }

    public Integer readWrapInteger(NamespacedKey key) {
        return readObject(key, Integer.class);
    }

    public Integer readWrapInteger(String key) {
        return readWrapInteger(key(key));
    }

    public int readInteger(NamespacedKey key) {
        Integer i = readWrapInteger(key);
        return i == null ? 0 : i;
    }
    
    public int readInteger(String key) {
        return readInteger(key(key));
    }

    public Long readWrapLong(NamespacedKey key) {
        return readObject(key, Long.class);
    }

    public Long readWrapLong(String key) {
        return readWrapLong(key(key));
    }

    public long readLong(NamespacedKey key) {
        Long l = readWrapLong(key);
        return l == null ? 0 : l;
    }
    
    public long readLong(String key) {
        return readLong(key(key));
    }

    public Float readWrapFloat(NamespacedKey key) {
        return readObject(key, Float.class);
    }

    public Float readWrapFloat(String key) {
        return readWrapFloat(key(key));
    }

    public float readFloat(NamespacedKey key) {
        Float f = readWrapFloat(key);
        return f == null ? 0 : f;
    }
    
    public float readFloat(String key) {
        return readFloat(key(key));
    }

    public Double readWrapDouble(NamespacedKey key) {
        return readObject(key, Double.class);
    }

    public Double readWrapDouble(String key) {
        return readWrapDouble(key(key));
    }

    public double readDouble(NamespacedKey key) {
        Double d = readWrapDouble(key);
        return d == null ? 0 : d;
    }
    
    public double readDouble(String key) {
        return readDouble(key(key));
    }

    public Boolean readWrapBoolean(NamespacedKey key) {
        return readObject(key, Boolean.class);
    }

    public Boolean readWrapBoolean(String key) {
        return readWrapBoolean(key(key));
    }

    public boolean readBoolean(NamespacedKey key) {
        Boolean b = readWrapBoolean(key);
        return b == null ? false : b;
    }
    
    public boolean readBoolean(String key) {
        return readBoolean(key(key));
    }

    public Character readWrapCharacter(NamespacedKey key) {
        return readObject(key, Character.class);
    }

    public Character readWrapCharacter(String key) {
        return readWrapCharacter(key(key));
    }

    public char readCharacter(NamespacedKey key) {
        Character c = readWrapCharacter(key);
        return c == null ? '\u0000' : c;
    }
    
    public char readCharacter(String key) {
        return readCharacter(key(key));
    }

    public String readString(NamespacedKey key) {
        return readObject(key, String.class);
    }

    public String readString(String key) {
        return readString(key(key));
    }

    public UUID readUUID(NamespacedKey key) {
        return readObject(key, UUID.class);
    }

    public UUID readUUID(String key) {
        return readUUID(key(key));
    }

    public BigInteger readBigInteger(NamespacedKey key) {
        return readObject(key, BigInteger.class);
    }

    public BigInteger readBigInteger(String key) {
        return readBigInteger(key(key));
    }

    public BigDecimal readBigDecimal(NamespacedKey key) {
        return readObject(key, BigDecimal.class);
    }

    public BigDecimal readBigDecimal(String key) {
        return readBigDecimal(key(key));
    }

    public Object readObject(NamespacedKey key) {
        var list = getDefaultDataTypes();
        var container = getContainer();
        Object found = null;
        for(var type : list) {
            try {
                found = container.get(key, type);
                if(found == null) {
                    continue;
                }
                break;
            } catch(IllegalArgumentException e) {
                continue;
            }
        }
        if(found instanceof byte[] array) {
            try {
                return Serializer.deserialize(array);
            } catch(Exception e) {
                Throwables.sendAndThrow("PersistentDataManager/readObject", e);
            }
        }
        return found;
    }

    public Object readObject(String key) {
        return readObject(key(key));
    }

    public void writeObject(NamespacedKey key, Object object) {
        try {
            T holder = getHolder();
            var container = holder.getPersistentDataContainer();
            container.set(key, PersistentDataType.BYTE_ARRAY, Serializer.serialize(object));
            if(holder instanceof TileState state) state.update();
        } catch(Exception e) {
            Throwables.sendAndThrow("PersistentDataManager/writeObject", e);
        }
    }

    public void writeObject(String key, Object n) {
        writeObject(key(key), n);
    }

    public <U> U readObject(NamespacedKey key, Class<U> componentType) {
        return componentType.cast(readObject(key));
    }

    public <U> U readObject(String key, Class<U> componentType) {
        return readObject(key(key), componentType);
    }

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
            PersistentDataType.LONG_ARRAY
        );
    }

    public static NamespacedKey key(String str) {
        if(str == null) return null;
        int len = str.length();
        if(len == 0 || len >= 256) {
            throw new IllegalArgumentException("NamespacedKey must have length greater than 0 and smaller than 256");
        }
        String namespace, key;
        if(!str.contains(":")) {
            namespace = NamespacedKey.MINECRAFT;
            key = str;
        } else {
            String[] split = str.split(":");
            if(split.length > 2) throw new IllegalArgumentException("The String must be in format 'namespace:key'. String:" + str);
            namespace = split[0];
            key = split[1];
            if(!namespace.matches("[a-z0-9._-]+")) throw new IllegalArgumentException("Invalid namespace. Must be [a-z0-9._-]: " + namespace);
            if(!key.matches("[a-z0-9/._-]+")) throw new IllegalArgumentException("Invalid key. Must be [a-z0-9/._-]: " + key);
        }
        return new NamespacedKey(namespace, key);
    }

}
