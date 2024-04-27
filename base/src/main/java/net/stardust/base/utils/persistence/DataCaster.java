package net.stardust.base.utils.persistence;

import org.bukkit.persistence.PersistentDataHolder;

public interface DataCaster<T, U extends PersistentDataHolder> {
    
    T cast(DataManager<U> dataManager) throws NonRepresentativeDataException;
    void record(T obj, DataManager<U> dataManager);

}
