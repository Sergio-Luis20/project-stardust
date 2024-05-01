package net.stardust.minigames.capture;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;

import java.util.Objects;

public class CaptureMatchListener implements Listener {

    private Capture capture;

    public CaptureMatchListener(Capture capture) {
        this.capture = Objects.requireNonNull(capture, "capture");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlacing(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onTryingToScape(PlayerMoveEvent event) {
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

    @EventHandler
    public void onEnteringOwnBase(PlayerMoveEvent event) {
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

    @EventHandler
    public void handleVoidDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && event.getCause() == DamageCause.VOID) {
            event.setCancelled(true);
            capture.dismountAll(player);
            capture.captured(null, player);
        }
    }

    @EventHandler
    public void preventDamageByOthers(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        if(capture.isCaptured(damager) || capture.isCaptured(victim)
                || capture.isBeingCarried(damager) || capture.isBeingCarried(victim)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDying(PlayerDeathEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        capture.dismountAll(player);
        capture.mount(player.getKiller(), player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Player princess) {
            Player hero = event.getPlayer();
            if(CaptureTeam.areSameTeam(capture, hero, princess) && capture.isCaptured(princess)
                    && !capture.isCaptured(hero)) {
                capture.mount(hero, princess);
            }
        }
    }

    @EventHandler
    public void preventDismount(EntityDismountEvent event) {
        if(event.getEntity() instanceof Player player) {
            event.setCancelled(true);
            player.sendMessage(Component.translatable("minigame.capture.cant-escape", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        capture.onDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onQuittingMatch(PlayerTeleportEvent event) {
        if(!event.getTo().getWorld().equals(capture.getWorld())) {
            capture.onQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void removeArrow(ProjectileHitEvent event) {
        event.getEntity().remove();
    }

    @EventHandler
    public void preserveDurability(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventDroppingItems(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventPickingUpItems(PlayerPickItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void preventReceiveXP(PlayerPickupExperienceEvent event) {
        event.setCancelled(true);
        event.getExperienceOrb().remove();
    }

}
