package net.stardust.base.utils.persistence;

import org.bukkit.persistence.PersistentDataHolder;

public class SimpleDataManager extends DataManager<PersistentDataHolder> {
    
    public SimpleDataManager(PersistentDataHolder holder) {
        super(holder);
    }

}
