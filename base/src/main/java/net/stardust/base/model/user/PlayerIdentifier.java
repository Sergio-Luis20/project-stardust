package net.stardust.base.model.user;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.EqualsAndHashCode.Exclude;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.Identifier;

@Getter
@EqualsAndHashCode
public class PlayerIdentifier implements Identifier<UUID>, Comparable<PlayerIdentifier> {
    
    private final UUID id;

    @Exclude
    private transient Player player;

    @Exclude
    private transient OfflinePlayer offlinePlayer;

    public PlayerIdentifier(UUID id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public PlayerIdentifier(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }

    public PlayerIdentifier(OfflinePlayer offlinePlayer) {
        this(offlinePlayer.getUniqueId());
        this.offlinePlayer = offlinePlayer;
        if (offlinePlayer instanceof Player player) {
            this.player = player;
        }
    }

    @Override
    public String getStringName() {
        return getOfflinePlayer().getName();
    }

    @Override
    public Component getComponentName() {
        return Component.text(getStringName());
    }

    @Override
    public int compareTo(PlayerIdentifier identifier) {
        return id.compareTo(identifier.id);
    }

    public Player getPlayer() {
        if(player == null) {
            player = Bukkit.getPlayer(id);
            if (player != null) {
                offlinePlayer = player;
            }
        }
        return player;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(id);
            if (offlinePlayer instanceof Player player) {
                this.player = player;
            }
        }
        return offlinePlayer;
    }
    
    public Player checkPlayer() {
        Player player = getPlayer();
        if (player == null) {
            throw new NullPointerException("Player does not exist or is offline");
        }
        return player;
    }

}
