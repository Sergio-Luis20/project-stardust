package net.stardust.base.model.inventory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.bukkit.NamespacedKey;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@JacksonXmlRootElement(localName = "tag")
@NoArgsConstructor
public class PersistentObject implements Serializable, Cloneable {
    
    @JsonUnwrapped
    @JacksonXmlProperty(localName = "namespacedKey")
    private NamespacedKey namespacedKey;
    private Object value;

    public PersistentObject(NamespacedKey namespacedKey, Object value) {
        this.namespacedKey = Objects.requireNonNull(namespacedKey, "namespacedKey");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public PersistentObject clone() {
        NamespacedKey key = NamespacedKey.fromString(namespacedKey.asString());
        Object newValue = value;
        if(value instanceof Cloneable) {
            try {
                Method clone = value.getClass().getDeclaredMethod("clone");
                newValue = clone.invoke(value);
            } catch(Exception e) {
                throw new Error(e);
            }
        }
        return new PersistentObject(key, newValue);
    }

    public String valueToString() {
        if(value instanceof byte[] array) return Arrays.toString(array);
        if(value instanceof short[] array) return Arrays.toString(array);
        if(value instanceof int[] array) return Arrays.toString(array);
        if(value instanceof long[] array) return Arrays.toString(array);
        if(value instanceof float[] array) return Arrays.toString(array);
        if(value instanceof double[] array) return Arrays.toString(array);
        if(value instanceof boolean[] array) return Arrays.toString(array);
        if(value instanceof char[] array) return Arrays.toString(array);
        if(value instanceof Object[] array) return Arrays.deepToString(array);
        return String.valueOf(value);
    }

}
