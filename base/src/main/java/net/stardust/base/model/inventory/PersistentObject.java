package net.stardust.base.model.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.stardust.base.Stardust;
import org.bukkit.NamespacedKey;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class PersistentObject implements Serializable, Cloneable {
    
    private NamespacedKey namespacedKey;
    private Object value;

    @JsonCreator
    public PersistentObject(@JsonProperty(value = "namespacedKey", required = true) NamespacedKey namespacedKey,
                            @JsonProperty(value = "value", required = true) Object value) {
        this.namespacedKey = Objects.requireNonNull(namespacedKey, "namespacedKey");
        this.value = Objects.requireNonNull(value, "value");
    }

    public <T> T getObject(Class<T> componentType) {
        return componentType.cast(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof PersistentObject obj) {
            return namespacedKey.equals(obj.namespacedKey) && Stardust.equals(value, obj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespacedKey, Stardust.hashCode(value));
    }

    @Override
    public String toString() {
        return "PersistentObject{" +
                "namespacedKey=" + namespacedKey +
                ", value=" + Stardust.toString(value) +
                '}';
    }

    @Override
    public PersistentObject clone() {
        NamespacedKey key = Stardust.key(namespacedKey.asString());
        Object newValue = value;
        if(value.getClass().isArray() || value instanceof Cloneable) {
            try {
                Method clone = value.getClass().getDeclaredMethod("clone");
                newValue = clone.invoke(value);
            } catch(Exception e) {
                throw new Error(e);
            }
        }
        return new PersistentObject(key, newValue);
    }

}
