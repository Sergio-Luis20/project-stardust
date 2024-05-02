package net.stardust.minigames.capture;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.events.WorldListener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;

import java.util.Objects;

public class CaptureMatchListener extends WorldListener {

    private Capture capture;

    public CaptureMatchListener(Capture capture) {
        super(capture::getWorld);
        this.capture = Objects.requireNonNull(capture, "capture");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlacing(BlockPlaceEvent event) {
        if(checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTryingToScape(PlayerMoveEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            Player player = event.getPlayer();
            if(!capture.isCaptured(player)) {
                return;
            }
            CaptureTeam enemy = CaptureTeam.getTeam(capture, player).other();
            if(enemy.isOutsideBase(capture, player) && !capture.isBeingCarried(player)) {
                player.teleport(enemy.getBase(capture));
                player.sendMessage(Component.translatable("minigame.capture.cant-escape", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onEnteringOwnBase(PlayerMoveEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            Player player = event.getPlayer();
            // Prevent bugs when being saved
            if(capture.isCaptured(player)) {
                return;
            }
            CaptureTeam team = CaptureTeam.getTeam(capture, player);
            if(team.isInsideBase(capture, player)) {
                capture.deliveryCarry(player);
            }
        }
    }

    @EventHandler
    public void handleVoidAndFireDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && checkWorld(player.getWorld())) {
            DamageCause cause = event.getCause();
            if(cause == DamageCause.VOID) {
                event.setCancelled(true);
                capture.dismountAll(player);
                capture.captured(null, player);
            } else if((cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK
                    && cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.PROJECTILE) || capture.isEnding()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void preventDamageByOthers(EntityDamageByEntityEvent event) {
        if(checkWorld(event.getEntity().getWorld())) {
            if(!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim)) {
                return;
            }
            if(capture.isCaptured(damager) || capture.isCaptured(victim)
                    || capture.isBeingCarried(damager) || capture.isBeingCarried(victim)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDying(PlayerDeathEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            capture.dismountAll(player);
            capture.mount(player.getKiller(), player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Player princess && checkWorld(princess.getWorld())) {
            Player hero = event.getPlayer();
            if(CaptureTeam.areSameTeam(capture, hero, princess) && capture.isCaptured(princess)
                    && !capture.isCaptured(hero)) {
                capture.mount(hero, princess);
            }
        }
    }

    @EventHandler
    public void preventDismount(EntityDismountEvent event) {
        if(event.getEntity() instanceof Player player && checkWorld(player.getWorld()) && capture.isBeingCarried(player)) {
            event.setCancelled(true);
            player.sendMessage(Component.translatable("minigame.capture.cant-escape", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            capture.leave(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuittingMatch(PlayerTeleportEvent event) {
        if(checkWorld(event.getFrom().getWorld()) && !checkWorld(event.getTo().getWorld())) {
            capture.onQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void removeArrow(ProjectileHitEvent event) {
        Entity entity = event.getEntity();
        if(checkWorld(entity.getWorld())) {
            entity.remove();
        }
    }

    @EventHandler
    public void preserveDurability(PlayerItemDamageEvent event) {
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
    public void preventPickingUpItems(PlayerPickItemEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventReceiveXP(PlayerPickupExperienceEvent event) {
        if(checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            event.getExperienceOrb().remove();
        }
    }

    @EventHandler
    public void preventHunger(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player player && checkWorld(player.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventSpawningEntities(EntitySpawnEvent event) {
        if(checkWorld(event.getLocation().getWorld()) && event.getEntityType() != EntityType.ARROW) {
            event.setCancelled(true);
        }
    }

}
