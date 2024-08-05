package net.stardust.base.utils.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

/**
 * <p>
 * This interface serves to define a contract
 * in which the implementing class must return
 * a map containing the type definitions for
 * each key.
 * <p>
 * 
 * <p>
 * For example: if a key mapper is defined for
 * the type <b>Person</b>, which is represented by the data
 * <b>
 * <p>
 * Integer age
 * <p>
 * String name
 * <p>
 * Float height
 * </b>
 * <p>
 * then the mapper for a Person should return a {@link Map} object
 * that contains entries like these:
 * <b>
 * <p>
 * stardust:person_age -> Integer.class<br>
 * <p>
 * stardust:person_name -> String.class<br>
 * <p>
 * stardust:person_height -> Float.class<br>
 * </b>
 * 
 * <p>
 * Note that types such as int.class and Integer.class, for example,
 * differ in the nature of the class, and this may potentially impact
 * the results.
 * </p>
 * 
 */
@FunctionalInterface
public interface KeyMapper {

    /**
     * <p>
     * Returns the map defining the structure of a "data class" of an object
     * stored in a {@link PersistentDataContainer}. Neighter keys nor values
     * of the returned map should be null, and the returned map itself must
     * also be not null and not empty.
     * </p>
     * 
     * <p>
     * It is not recommended using <b>minecraft</b> as a key of a
     * {@link NamespacedKey} inside this map. For more details on how the
     * returned map should look like, see {@link KeyMapper} documentation.
     * </p>
     * 
     * @see KeyMapper
     * @see Map
     * @see NamespacedKey
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @return the "data class" in a {@link Map} form
     */
    Map<NamespacedKey, Class<?>> getKeyMap();

    /**
     * Works the same way as {@link #getKeyMap()}, but the keys are the String
     * representation of the {@link NamespacedKey}s returned by it. Do not override
     * this method unless there is a very good reason.
     * 
     * @see #getKeyMap()
     * @return the key map with keys as String
     * @throws NullPointerException if the map returned by {@link #getKeyMap()}
     *                              doesn't
     *                              follow the contract of that method about nullity
     *                              of the map itself or its entries
     */
    default Map<String, Class<?>> getKeyMapAsString() {
        Map<NamespacedKey, Class<?>> map = getKeyMap();
        if (map == null)
            throw new NullPointerException("null map returned by KeyMapper#getKeyMap()");
        Map<String, Class<?>> newMap = new HashMap<>();
        map.forEach((nsk, c) -> {
            if (nsk == null)
                throw new NullPointerException("null NamespacedKey in a key mapper map");
            if (c == null)
                throw new NullArgumentException("null Class in a key mapper map");
            newMap.put(nsk.asString(), c);
        });
        return newMap;
    }

    /**
     * <p>
     * Checks if the {@link PersistentDataContainer} matches the preset
     * structure inside the given {@link KeyMapper}. As this works like a
     * "data interface", if the {@link PersistentDataContainer} has mappings
     * for all keys specified in the {@link KeyMapper} and the objects inside
     * matches the classes associated with the respective {@link NamespacedKey},
     * then the {@link PersistentDataHolder} instance "is" an object represented
     * by the "class" in map form given by the {@link KeyMapper}. The classes
     * in the {@link Map} returned by the {@link KeyMapper} can be not exactly
     * the same class as the stored object, it can be a superclass or
     * superinterface.
     * </p>
     * 
     * @implNote The internal implementation calls
     *           {@link DataManager#readObject(NamespacedKey)} for every key the in
     *           the
     *           {@link Map} returned by the {@link KeyMapper} parameter for class
     *           checking, so keep in mind that depending on the type and data size
     *           of
     *           the stored keys, this can have a terrible performance and should be
     *           used with care.
     * 
     * @see KeyMapper
     * @see KeyMapper#getKeyMap()
     * @see PersistentDataHolder
     * @see PersistentDataContainer
     * @see NamespacedKey
     * @see DataManager
     * @see DataManager#readObject(NamespacedKey)
     * @param keyMapper   the mapper to check if the {@link PersistentDataHolder}
     *                    "is" an instance of the object represented by it
     * @param dataManager the wrapper for the {@link PersistentDataHolder}
     * @return {@code true} if the {@link PersistentDataHolder} "is" an object
     *         represented by the "class" in map form given by the
     *         {@link KeyMapper},
     *         {@code false} otherwise
     */
    static boolean check(KeyMapper keyMapper, DataManager<?> dataManager) {
        Map<NamespacedKey, Class<?>> map = keyMapper.getKeyMap();
        for (Entry<NamespacedKey, Class<?>> entry : map.entrySet()) {
            Object obj = dataManager.readObject(entry.getKey());
            if (obj == null || !entry.getValue().isAssignableFrom(obj.getClass())) {
                return false;
            }
        }
        return true;
    }

}
