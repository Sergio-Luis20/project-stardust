package net.stardust.repository.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.stardust.base.Communicable;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Throwables;
import net.stardust.repository.Repository;
import net.stardust.repository.RepositoryPlugin;

public abstract class MapRepository<K, V extends StardustEntity<K>> implements Repository<K, V>, Communicable {

    protected Logger log;
    protected ExecutorService flusher;
    protected RepositoryPlugin plugin;

    private Class<K> keyClass;
    private Class<V> valueClass;
    private String id;
    private Map<K, V> elements;

    public MapRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
        this.plugin = plugin;
        this.keyClass = keyClass;
        this.valueClass = valueClass;

        log = plugin.getLogger();
        flusher = plugin.getVirtual();
        id = plugin.getId() + "/" + valueClass.getSimpleName();

        elements = initializeElements();
    }
    
    protected abstract void doFlush(Map<K, V> elements);

    protected abstract Map<K, V> initializeElements();

    @Override
    public List<V> findAll() {
        synchronized (elements) {
            return new ArrayList<>(elements.values());
        }
    }

    @Override
    public List<V> findAll(List<K> list) {
        synchronized (elements) {
            List<V> resultList = new ArrayList<>(list.size());
            list.forEach(key -> resultList.add(elements.get(key)));
            return resultList;
        }
    }

    @Override
    public V findById(K id) {
        synchronized (elements) {
            return elements.get(id);
        }
    }

    @Override
    public boolean existsById(K id) {
        synchronized (elements) {
            return elements.containsKey(id);
        }
    }

    @Override
    public SaveResult save(V data, boolean update) {
        K key = data.getEntityId();
        if (update || !existsById(key)) {
            try {
                synchronized (elements) {
                    elements.put(key, data);
                }
            } catch (Exception e) {
                log.log(Level.SEVERE,
                        "Could not save an element into database. Repository class: %s. Key class: %s. Value class: %s"
                                .formatted(getClass().getName(), keyClass.getName(), valueClass.getName()),
                        e);
                Throwables.send(getId(), e);
                return SaveResult.FAIL;
            }
        } else {
            return SaveResult.DUPLICATE;
        }
        flush();
        return SaveResult.SUCCESS;
    }

    @Override
    public SaveResult saveAll(List<V> list, boolean update) {
        List<K> keys = list.stream().map(V::getEntityId).toList();
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            K key = keys.get(i);
            if (update || !existsById(key)) {
                try {
                    synchronized (elements) {
                        elements.put(key, list.get(i));
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE,
                            "could not save list of elements into database. Repository class: %s. Key class: %s. Value class: %s"
                                    .formatted(getClass().getName(), keyClass.getName(), valueClass.getName()),
                            e);
                    Throwables.send(getId(), e);
                    return SaveResult.FAIL;
                }
            } else {
                return SaveResult.DUPLICATE;
            }
        }
        flush();
        return SaveResult.SUCCESS;
    }

    @Override
    public boolean delete(K id) {
        try {
            synchronized (elements) {
                elements.remove(id);
            }
            flush();
            return true;
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "Could not delete the element of id \"%s\" from database. Repository class: %s. Key class: %s. Value class: %s"
                            .formatted(id, getClass().getName(), keyClass.getName(), valueClass.getName()),
                    e);
            Throwables.send(getId(), e);
            return false;
        }
    }

    @Override
    public boolean deleteAll(List<K> list) {
        try {
            synchronized (elements) {
                list.forEach(this.elements::remove);
            }
            flush();
            return true;
        } catch (Exception e) {
            Throwables.send(getId(), e);
            return false;
        }
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public Class<K> getKeyClass() {
        return keyClass;
    }

    @Override
    public Class<V> getValueClass() {
        return valueClass;
    }

    @Override
    public String getId() {
        return id;
    }

    public void flush() {
        flusher.submit(() -> doFlush(elements));
    }

    public RepositoryPlugin getPlugin() {
        return plugin;
    }

}
