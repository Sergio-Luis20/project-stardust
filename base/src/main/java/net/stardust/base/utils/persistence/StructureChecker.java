package net.stardust.base.utils.persistence;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.NamespacedKey;

import lombok.Data;
import lombok.NonNull;

/**
 * Class responsible for checking if the data structure of
 * a PersistentDataContainier matches the preset structure.
 * This works like a "data interface".
 * 
 * @author Sergio Luis
 */
@Data
public class StructureChecker {
    
    /**
     * The object responsible for give the "class" representation
     * in map form for object checkings.
     */
    @NonNull
    private KeyMapper keyMapper;

    /**
     * Checks if the PersistentDataContainer matches the preset
     * structure inside the given KeyMapper. As this works like a
     * "data interface", if the PersistentDataContainer has mappings
     * for all keys specified in the KeyMapper and the objects inside
     * matches the classes associated with the respective NamespacedKey,
     * then the PersistentDataHolder "is" an object represented by the
     * "class" in map form given by the KeyMapper.
     * @param dataManager the wrapper for the PersistentDataHolder.
     * @return {@code true} if the PersistentDataHolder "is" an object
     * represented by the "class" in map form given by the KeyMapper,
     * {@code false} otherwise.
     */
    public boolean check(DataManager<?> dataManager) {
        Map<NamespacedKey, Class<?>> map = keyMapper.getKeyMap();
        for(Entry<NamespacedKey, Class<?>> entry : map.entrySet()) {
            Object obj = dataManager.readObject(entry.getKey());
            if(obj == null || obj.getClass() != entry.getValue()) {
                return false;
            }
        }
        return true;
    }

}
