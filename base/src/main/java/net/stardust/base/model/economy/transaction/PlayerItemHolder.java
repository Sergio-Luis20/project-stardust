package net.stardust.base.model.economy.transaction;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.PlayerCash;
import net.stardust.base.model.economy.storage.InventoryStorage;
import net.stardust.base.model.economy.storage.Storage;

public class PlayerItemHolder extends PlayerCash implements ItemHolder {

    public PlayerItemHolder(UUID id) {
        super(id);
    }

    public PlayerItemHolder(Player player) {
        super(player);
    }

    public PlayerItemHolder(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    @Override
    public Storage getStorage() {
        return new InventoryStorage(checkPlayer().getInventory());
    }
    
}
