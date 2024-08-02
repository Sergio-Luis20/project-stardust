package net.stardust.base.model.economy;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.message.PlayerMessageable;

public class PlayerCash extends PlayerMessageable implements Cash {

    public PlayerCash(UUID id) {
        super(id);
    }

    public PlayerCash(Player player) {
        super(player);
    }

    public PlayerCash(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    @Override
    public PlayerWallet getWallet() {
        PlayerWalletCrud crud = new PlayerWalletCrud();
        return crud.getOrThrow(getId());
    }

}
