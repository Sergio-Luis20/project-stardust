package net.stardust.base.utils;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

public class KeyValuePair<K, V> implements Serializable, Cloneable {
    
    private final K key;
    private V value;

    public KeyValuePair(K key, V value) {
        this.key = Objects.requireNonNull(key, "key");
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V old = getValue();
        this.value = value;
        return old;
    }

    public Entry<K, V> asMapEntry() {
        return new KeyValuePairEntry<>(this);
    }

    public static class KeyValuePairEntry<K, V> implements Entry<K, V>, Serializable, Cloneable {

        private KeyValuePair<K, V> enclosed;

        public KeyValuePairEntry(KeyValuePair<K, V> enclosed) {
            this.enclosed = Objects.requireNonNull(enclosed, "enclosed");
        }

        @Override
        public K getKey() {
            return enclosed.getKey();
        }

        @Override
        public V getValue() {
            return enclosed.getValue();
        }

        @Override
        public V setValue(V value) {
            return enclosed.setValue(value);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(obj == this) return true;
            if(obj instanceof Entry<?, ?> entry) {
                return Objects.equals(getKey(), entry.getKey()) && Objects.equals(getValue(), entry.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }

        @Override
        public KeyValuePairEntry<K, V> clone() {
            return new KeyValuePairEntry<>(enclosed);
        }
        
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof KeyValuePair<?, ?> pair) {
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch(CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
