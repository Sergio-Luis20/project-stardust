package net.stardust.base.utils;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class CollectionMap<K, V, E extends KeyValuePair<K, V>> extends AbstractMap<K, V> {

    private Class<E> elementClass;
    private Class<K> keyClass;
    private Class<V> valueClass;

    private Collection<E> main;
    private Set<Entry<K, V>> entrySet;

    public CollectionMap(Collection<E> collection, Class<E> elementClass, Class<K> keyClass, Class<V> valueClass) {
        this.elementClass = Objects.requireNonNull(elementClass, "elementClass");
        this.keyClass = Objects.requireNonNull(keyClass, "keyClass");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        
        main = Objects.requireNonNull(collection, "collection");
        entrySet = new CollectionMapEntrySet();
    }

    public Collection<E> getMainCollection() {
        return main;
    }

    @Override
    public V put(K key, V value) {
        Collection<E> main = getMainCollection();
        Objects.requireNonNull(key);
        Entry<K, V> entry = getEntry(key);
        if(value == null) {
            if(entry == null) {
                return null;
            }
            main.remove(newInstance(key, value));
            return entry.getValue();
        } else {
            if(entry == null) {
                main.add(newInstance(key, value));
                return null;
            }
            V oldValue = entry.getValue();
            entry.setValue(value);
            return oldValue;
        }
    }
    
    private E newInstance(K key, V value) {
        try {
            return elementClass.getConstructor(keyClass, valueClass).newInstance(key, value);
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("Your element class must have a public constructor with key and value as parameters respectively");
        } catch(Exception e) {
            if(e instanceof RuntimeException re) throw re;
            throw new RuntimeException(e);
        }
    }

    private Entry<K, V> getEntry(K key) {
        for(Entry<K, V> entry : entrySet) {
            if(entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    private class CollectionMapEntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public int size() {
            return getMainCollection().size();
        }

        @Override
        public boolean add(Entry<K, V> e) {
            throw new UnsupportedOperationException("Cannot add to map by its entry set");
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException("Cannot add all to map by its entry set");
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new FunctionIterator<>(getMainCollection().iterator(), KeyValuePair::asMapEntry);
        }
        
    }
    
}
