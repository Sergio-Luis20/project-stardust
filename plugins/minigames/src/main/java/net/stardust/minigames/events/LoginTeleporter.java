package net.stardust.minigames.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.stardust.base.events.BaseListener;

@BaseListener
public class LoginTeleporter implements Listener {
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        teleportPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teleportPlayer(event.getPlayer());
    }

    private void teleportPlayer(Player player) {
        if(player.getWorld().getName().startsWith("minigame")) {
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }

}
