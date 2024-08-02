package net.stardust.base.model.economy;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.utils.Messageable;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;

@Getter
public class PlayerCash implements Cash, Messageable, Serializable, Cloneable, Comparable<PlayerCash> {

    private UUID id;
    private transient Player player;
    private transient OfflinePlayer offlinePlayer;

    public PlayerCash(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public PlayerCash(Player player) {
        this(player.getUniqueId());
        this.player = player;
        this.offlinePlayer = player;
    }

    public PlayerCash(OfflinePlayer offlinePlayer) {
        this(offlinePlayer.getUniqueId());
        this.offlinePlayer = offlinePlayer;
        if (offlinePlayer instanceof Player player) {
            this.player = player;
        }
    }

    @Override
    public void sendMessage(Component component) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(component);
        }
    }

    @Override
    public PlayerWallet getWallet() {
        PlayerWalletCrud crud = new PlayerWalletCrud();
        return crud.getOrThrow(id);
    }

    public Player getPlayer() {
        if (player == null) {
            player = Bukkit.getPlayer(id);
        }
        return player;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(id);
        }
        return offlinePlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof PlayerCash cash) {
            return id.equals(cash.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(PlayerCash cash) {
        return id.compareTo(cash.id);
    }

    @Override
    public PlayerCash clone() {
        PlayerCash clone = new PlayerCash(id);
        clone.player = player;
        clone.offlinePlayer = offlinePlayer;
        return clone;
    }

}
