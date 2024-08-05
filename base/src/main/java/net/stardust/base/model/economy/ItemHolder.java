package net.stardust.base.model.economy;

import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemNegotiators;
import net.stardust.base.model.economy.transaction.ItemTransaction;

/**
 * Represents an entity being able to trade items, that is,
 * perform {@link ItemTransaction}s.
 * 
 * @see ItemTransaction
 * @see ItemNegotiators
 * 
 * @author Sergio Luis
 */
public interface ItemHolder extends Cash {
    
    /**
     * Returns the {@link Storage} of this entity.
     * 
     * @see Storage
     * @return the storage of this entity
     */
    Storage getStorage();

}
