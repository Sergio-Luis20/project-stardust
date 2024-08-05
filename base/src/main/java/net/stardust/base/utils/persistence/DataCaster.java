package net.stardust.base.utils.persistence;

import org.bukkit.persistence.PersistentDataHolder;

/**
 * Represents an object that can serialize and deserialize
 * another type of object in a {@link DataManager}.
 * 
 * @see DataManager
 * 
 * @author Sergio Luis
 */
public interface DataCaster<T, U extends PersistentDataHolder> {

    /**
     * Deserializes an object using the passed {@link DataManager} as
     * parameter. If the object inside the {@link PersistentDataContainer}
     * of the {@link PersistentDataHolder} wrapped by the data manager "is not"
     * an object of the requested type or there is no mapping for an object
     * with the same keys inside the container, a
     * {@link NonRepresentativeDataException}
     * is thrown. You can know more details about this "being" concept in
     * {@link KeyMapper} documentation.
     * 
     * @see KeyMapper
     * @see DataManager
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @see NonRepresentativeDataException
     * @param dataManager the data manager from where deserialize the object
     * @return the deserialized object
     * @throws NonRepresentativeDataException if the there is no mapping for keys
     *                                        inside the container or the respective
     *                                        object of the mapped keys is not of
     *                                        the requested type
     */
    T cast(DataManager<U> dataManager) throws NonRepresentativeDataException;

    /**
     * Serializes an object using the passed {@link DataManager} as parameter.
     * If there is already some object in the {@link PersistentDataContainer}
     * of the {@link PersistentDataHolder} wrapped by the data manager using
     * the same keys of this object, those keys will be overridden. If the
     * passed object is null, then the keys associated to its type inside the
     * container will be erased.
     * 
     * @param obj         the object to serialize
     * @param dataManager the data manager to where serialize the object
     */
    void record(T obj, DataManager<U> dataManager);

}
