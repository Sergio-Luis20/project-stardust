package net.stardust.base.model.economy.transaction;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.stardust.base.model.economy.storage.Storage;

/**
 * Subclass of {@link PlayerItemHolder} that overrides {@link #getStorage()}
 * to return a custom {@link Storage} object other than the default one in
 * the superclass.
 * 
 * @see PlayerItemHolder
 * @see PlayerItemHolder#getStorage()
 * 
 * @author Sergio Luis
 */
public class AlternativeStoragePlayerItemHolder extends PlayerItemHolder {

    private Storage storage;

    /**
     * Creates a new {@link AlternativeStoragePlayerItemHolder} with the player id
     * and the alternative storage passed via parameters.
     * 
     * @param id the player id
     * @param storage the alternative storage
     * @throws NullPointerException if id or storage are null
     */
    public AlternativeStoragePlayerItemHolder(UUID id, Storage storage) {
        super(id);
        setStorage(storage);
    }

    /**
     * Creates a new {@link AlternativeStoragePlayerItemHolder} with the player
     * and the alternative storage passed via parameters.
     * 
     * @param player the player
     * @param storage the alternative storage
     * @throws NullPointerException if player or storage are null
     */
    public AlternativeStoragePlayerItemHolder(Player player, Storage storage) {
        this(player.getUniqueId(), storage);
    }

    /**
     * Returns the alternative {@link Storage} supplied via construction or setter.
     * This is never null.
     * 
     * @see Storage
     * @see ItemHolder
     * @see PlayerItemHolder
     * @return the alternative storage
     */
    @Override
    public Storage getStorage() {
        return storage;
    }

    /**
     * Sets the alternative {@link Storage} object. Cannot be null.
     * 
     * @param storage the alternative storage
     * @throws NullPointerException if storage is null
     */
    public void setStorage(Storage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }
    
}
