package net.stardust.base.model.economy;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.economy.wallet.Wallet;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.database.NotFoundException;
import net.stardust.base.utils.database.crud.Crud;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.message.PlayerMessageable;

/**
 * Class that represents a player that can perform {@link Transaction}s.
 * 
 * @see Transaction
 * @see Cash
 * @see PlayerMessageable
 * @see PlayerIdentifier
 * 
 * @author Sergio Luis
 */
public class PlayerCash extends PlayerMessageable implements Cash {

    /**
     * Constructs a new {@link PlayerCash} with the provided
     * player id.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(UUID)
     * @see PlayerMessageable#PlayerMessageable(UUID)
     * @param id the player id
     * @throws NullPointerException if id is null
     */
    public PlayerCash(UUID id) {
        super(id);
    }

    /**
     * Constructs a new {@link PlayerCash} with the provided
     * player.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(Player)
     * @see PlayerMessageable#PlayerMessageable(Player)
     * @param player the player
     * @throws NullPointerException if player is null
     */
    public PlayerCash(Player player) {
        super(player);
    }

    /**
     * Constructs a new {@link PlayerCash} with the provided
     * offline player. Check {@link PlayerIdentifier#PlayerIdentifier(OfflinePlayer)}
     * documentation for important details.
     * 
     * @see PlayerIdentifier#PlayerIdentifier(OfflinePlayer)
     * @see PlayerMessageable#PlayerMessageable(OfflinePlayer)
     * @param offlinePlayer the offline player
     * @throws NullPointerException if offline player is null
     */
    public PlayerCash(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    /**
     * Returns the wallet of the player of the id obtained
     * via construction. If the id is not an id of a Stardust
     * server player, that is, it is not in database, then this
     * method throws a {@link NotFoundException}.
     * 
     * @see PlayerWallet
     * @see PlayerWalletCrud
     * @see Wallet
     * @see Cash
     * @see Crud
     * @return the {@link PlayerWallet} object of the player
     * @throws NotFoundException if player is not in database
     */
    @Override
    public PlayerWallet getWallet() {
        PlayerWalletCrud crud = new PlayerWalletCrud();
        return crud.getOrThrow(getId());
    }

}
