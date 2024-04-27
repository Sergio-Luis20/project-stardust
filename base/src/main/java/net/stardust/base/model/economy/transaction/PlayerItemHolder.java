package net.stardust.base.model.economy.transaction;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.storage.InventoryStorage;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.wallet.Wallet;
import net.stardust.base.utils.Messageable;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;

public class PlayerItemHolder implements ItemHolder, Messageable {

    private UUID id;
    private Player player;

    public PlayerItemHolder(Player player) {
        this.player = Objects.requireNonNull(player, "player");
        id = player.getUniqueId();
    }

    public PlayerItemHolder(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    @Override
    public Wallet getWallet() {
        PlayerWalletCrud crud = new PlayerWalletCrud();
        return crud.getOrThrow(id);
    }

    @Override
    public Storage getStorage() {
        return new InventoryStorage(player.getInventory());
    }

    @Override
    public void sendMessage(Component message) {
        getPlayer().sendMessage(message);
    }

    public UUID getPlayerId() {
        return id;
    }

    public Player getPlayer() {
        if(player == null) {
            player = Bukkit.getPlayer(id);
        }
        return player;
    }
    
}
