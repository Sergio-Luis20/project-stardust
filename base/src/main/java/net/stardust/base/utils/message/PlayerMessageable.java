package net.stardust.base.utils.message;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.stardust.base.model.user.PlayerIdentifier;

public class PlayerMessageable extends PlayerIdentifier implements Messageable {

    public PlayerMessageable(UUID id) {
        super(id);
    }

    public PlayerMessageable(Player player) {
        super(player);
    }

    public PlayerMessageable(OfflinePlayer offlinePlayer) {
        super(offlinePlayer);
    }

    @Override
    public void sendMessage(Component component) {
        Player player = getPlayer();
        if (player != null) {
            player.sendMessage(component);
        }
    }
    
}
