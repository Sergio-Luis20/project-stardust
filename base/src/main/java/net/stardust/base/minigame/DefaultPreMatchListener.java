package net.stardust.base.minigame;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import java.util.Objects;

@Getter
public class DefaultPreMatchListener implements Listener {

    private Minigame parent;

    public DefaultPreMatchListener(Minigame parent) {
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    @EventHandler
    public void preventBreakingBlocks(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventPlacingBlocks(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventDying(PlayerDeathEvent event) {
        event.setCancelled(true);
        event.getPlayer().teleport(parent.getWorld().getSpawnLocation());
    }

    @EventHandler
    public void preventTakingDamage(EntityDamageEvent event) {
        event.setCancelled(true);
        if(event.getCause() == DamageCause.VOID) {
            event.getEntity().teleport(parent.getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void preventReceivingPotionEffects(EntityPotionEffectEvent event) {
        if(event.getEntity() instanceof Player player) {
            event.setCancelled(true);
            player.clearActivePotionEffects();
        }
    }

    @EventHandler
    public void preventPickingItems(PlayerPickItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventDroppingItems(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventPickingArrows(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventPickingExperience(PlayerPickupExperienceEvent event) {
        event.setCancelled(true);
    }

}
