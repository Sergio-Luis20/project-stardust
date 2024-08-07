package net.stardust.base.events;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.function.Supplier;

@Getter
public class DefaultListener extends WorldListener {

    public DefaultListener(Supplier<World> worldSupplier) {
        super(worldSupplier);
    }

    @EventHandler
    public void preventBreakingBlocks(BlockBreakEvent event) {
        if(checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventPlacingBlocks(BlockPlaceEvent event) {
        if(checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventDying(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if(checkWorld(player.getWorld())) {
            event.setCancelled(true);
            event.getPlayer().teleport(getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void preventTakingDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && checkWorld(player.getWorld())) {
            event.setCancelled(true);
            if(event.getCause() == DamageCause.VOID) {
                event.getEntity().teleport(getWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void preventItemDamage(PlayerItemDamageEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventHunger(FoodLevelChangeEvent event) {
        if(checkWorld(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventSpawningEntities(EntitySpawnEvent event) {
        if(checkWorld(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventPickingItems(PlayerPickItemEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventDroppingItems(PlayerDropItemEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventPickingArrows(PlayerPickupArrowEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventPickingExperience(PlayerPickupExperienceEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventWeatherChange(WeatherChangeEvent event) {
        if(checkWorld(event.getWorld())) {
            event.setCancelled(true);
        }
    }

}
