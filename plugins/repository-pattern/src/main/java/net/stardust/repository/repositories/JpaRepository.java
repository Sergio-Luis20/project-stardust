package net.stardust.repository.repositories;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import net.stardust.base.Communicable;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Throwables;
import net.stardust.repository.Repository;
import net.stardust.repository.RepositoryPlugin;

public class JpaRepository<K, V extends StardustEntity<K>> implements Repository<K, V>, Communicable {
    
    private Class<K> keyClass;
    private Class<V> valueClass;
    private String id;
    private EntityManager entityManager;
    private Logger log;

    public JpaRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = Objects.requireNonNull(keyClass, "keyClass");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
        id = plugin.getId() + "/" + valueClass.getSimpleName();
        entityManager = plugin.getEntityManagerFactory().createEntityManager();
        log = plugin.getLogger();
    }

    @Override
    public List<V> findAll() {
        var query = entityManager.createQuery("SELECT e FROM " + valueClass.getSimpleName() + " e", valueClass);
        return query.getResultList();
    }

    @Override
    public List<V> findAll(List<K> list) {
        var query = entityManager.createQuery("SELECT e FROM " + valueClass.getSimpleName() + " e WHERE e.id IN :ids",
                valueClass);
        query.setParameter("ids", list);
        return query.getResultList();
    }

    @Override
    public V findById(K id) {
        return entityManager.find(valueClass, id);
    }

    @Override
    public boolean existsById(K id) {
        var query = entityManager
                .createQuery("SELECT COUNT(e) FROM " + valueClass.getSimpleName() + " e WHERE e.id = :id", Long.class);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }

    @Override
    public SaveResult save(V data, boolean update) {
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            if (update) {
                entityManager.merge(data);
            } else {
                entityManager.persist(data);
            }
            transaction.commit();
            return SaveResult.SUCCESS;
        } catch (PersistenceException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return update ? SaveResult.FAIL : SaveResult.DUPLICATE;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while trying to save an entity into the database",
                    Throwables.send(getId(), e));
            return SaveResult.FAIL;
        }
    }

    @Override
    public SaveResult saveAll(List<V> list, boolean update) {
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            if (update) {
                for (V data : list) {
                    entityManager.merge(data);
                }
            } else {
                List<K> ids = list.stream().map(V::getEntityId).toList();
                var query = entityManager.createQuery(
                        "SELECT e.id FROM " + valueClass.getSimpleName() + " e WHERE e.id IN :ids", keyClass);
                query.setParameter("ids", ids);
                List<K> existingIds = query.getResultList();
                if (!existingIds.isEmpty()) {
                    transaction.rollback();
                    return SaveResult.DUPLICATE;
                }
                for (V data : list) {
                    entityManager.persist(data);
                }
            }
            transaction.commit();
            return SaveResult.SUCCESS;
        } catch (PersistenceException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            return update ? SaveResult.FAIL : SaveResult.DUPLICATE;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while trying to save a list of entities into the database",
                    Throwables.send(getId(), e));
            return SaveResult.FAIL;
        }
    }

    @Override
    public boolean delete(K id) {
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            V entity = entityManager.find(valueClass, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error while trying to delete an entity from the database",
                    Throwables.send(getId(), e));
            return false;
        }
    }

    @Override
    public boolean deleteAll(List<K> list) {
        var transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            for (K id : list) {
                V entity = entityManager.find(valueClass, id);
                if (entity != null) {
                    entityManager.remove(entity);
                }
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error while trying to delete a list of entities from the database",
                    Throwables.send(getId(), e));
            return false;
        }
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
    public void close() {
        entityManager.close();
    }

    @Override
    public String getId() {
        return id;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
    
}
