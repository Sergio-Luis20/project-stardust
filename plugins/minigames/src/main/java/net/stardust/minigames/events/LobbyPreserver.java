package net.stardust.minigames.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import net.stardust.base.events.BaseListener;

@BaseListener
public class LobbyPreserver implements Listener {
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        blockEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        blockEvent(event.getPlayer(), event);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if(event.getWorld().getName().endsWith("-lobby")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && player.getWorld().getName().endsWith("-lobby")) {
            event.setCancelled(true);
        }
    }

    private void blockEvent(Player player, Cancellable event) {
        World world = player.getWorld();
        if(world.getName().endsWith("-lobby")) {
            event.setCancelled(true);
        }
    }

}
