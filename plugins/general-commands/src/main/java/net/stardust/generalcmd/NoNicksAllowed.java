package net.stardust.generalcmd;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import net.stardust.base.database.lang.Translation;
import net.stardust.base.events.BaseListener;

@BaseListener
public class NoNicksAllowed implements Listener {

    private GeneralCommandsPlugin plugin;

    public NoNicksAllowed(GeneralCommandsPlugin plugin) {
        this.plugin = plugin;
    }
	
	@EventHandler
    public void onPreJoin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        if(plugin.getNotAllowedNicks().contains(name)) {
            event.disallow(Result.KICK_OTHER, Translation.get(player, "kick.invalid-nick"));
        }
    }
	
}
