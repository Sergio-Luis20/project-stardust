package net.stardust.base.model.economy.transaction;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.PlayerCash;
import net.stardust.base.model.economy.storage.InventoryStorage;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.user.InvalidPlayerException;
import net.stardust.base.model.user.PlayerIdentifier;

/**
 * Class that represents a player that is able to perform
 * an {@link ItemTransaction}.
 * 
 * @see ItemTransaction
 * @see ItemNegotiators
 * @see ItemHolder
 * 
 * @author Sergio Luis
 */
public class PlayerItemHolder extends PlayerCash implements ItemHolder {

    /**
     * Constructs a {@link PlayerItemHolder} instance with
     * the provided player id.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(UUID)
     * @see PlayerCash#PlayerCash(UUID)
     * @see Player#getUniqueId()
     * @param id the player id
     * @throws NullPointerException if id is null
     */
    public PlayerItemHolder(UUID id) {
        super(id);
    }

    /**
     * Constructs a {@link PlayerItemHolder} instance with
     * the provided player.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(Player)
     * @see PlayerCash#PlayerCash(Player)
     * @param player the player
     * @throws NullPointerException if player is null
     */
    public PlayerItemHolder(Player player) {
        super(player);
    }

    /**
     * Constructs a {@link PlayerItemHolder} instance with
     * the provided offline player. Check {@link PlayerIdentifier#PlayerIdentifier(OfflinePlayer)}
     * documentation for important details.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(OfflinePlayer)
     * @see PlayerCash#PlayerCash(OfflinePlayer)
     * @param offlinePlayer the offline player
     * @throws NullPointerException if offline player is null
     */
    public PlayerItemHolder(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    /**
     * Returns the {@link Storage} of this {@link ItemHolder}.
     * 
     * <p><b>Implementation note:</b> if the player of the respective
     * id object obtained via construction is not valid (check
     * {@link PlayerIdentifier#checkPlayer()} documentation for
     * this "valid" concept), this method will throw an {@link
     * InvalidPlayerException}.</p>
     * 
     * @see Storage
     * @see ItemHolder
     * @see PlayerIdentifier
     * @see PlayerIdentifier#checkPlayer()
     * @see InvalidPlayerException
     * @return the {@link Storage} of this {@link ItemHolder}
     * @throws InvalidPlayerException if the player is invalid
     */
    @Override
    public Storage getStorage() {
        return new InventoryStorage(checkPlayer().getInventory());
    }
    
}
