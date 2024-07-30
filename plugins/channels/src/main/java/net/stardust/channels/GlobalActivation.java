package net.stardust.channels;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.stardust.base.Communicable;
import net.stardust.base.model.channel.ChannelStatus;
import net.stardust.base.model.channel.Global;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.database.crud.ChannelStatusCrud;

public class GlobalActivation implements ChannelInventoryActivationClickHandler, Communicable {

    private Map<UUID, Boolean> activated;
    private ChannelsPlugin plugin;

    public GlobalActivation(ChannelsPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        activated = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isActivated(Player player) {
        return activated.getOrDefault(StardustThreads.call(plugin, () -> player.getUniqueId()), false);
    }

    @Override
    public void setActivated(Player player, boolean activated) {
        UUID playerId = StardustThreads.call(plugin, () -> player.getUniqueId());
        plugin.getVirtual().submit(() -> {
            this.activated.put(playerId, activated);
            ChannelStatusCrud crud = new ChannelStatusCrud();
            ChannelStatus status = crud.getOrThrow(playerId);
            String className = Global.class.getName();
            final String prop = "status";
            if (activated != status.isChannelActivated(className, prop)) {
                status.getProperties().get(className).put(prop, activated);
                crud.update(status);
            }
        });
    }

    @Override
    public String getId() {
        return plugin.getId() + "/" + getClass().getSimpleName();
    }
    
}
