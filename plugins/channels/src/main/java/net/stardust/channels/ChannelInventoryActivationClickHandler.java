package net.stardust.channels;

import org.bukkit.entity.Player;

public interface ChannelInventoryActivationClickHandler {
    boolean isActivated(Player player);
    void setActivated(Player player, boolean activated);
    String getChannelName();
}
