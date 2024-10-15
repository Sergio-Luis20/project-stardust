package net.stardust.minigames.capture;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.events.WorldListener;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
        if (checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlacing(BlockPlaceEvent event) {
        if (checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleMoving(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (checkWorld(player.getWorld())) {
            CaptureTeam team = CaptureTeam.getTeam(capture, player);
            if (capture.isCaptured(player)) {
                CaptureTeam enemy = team.other();
                if (enemy.isOutsideBase(capture, player) && !capture.isBeingCarried(player)) {
                    capture.teleportInWorld(player, enemy.getBase(capture));
                    player.sendMessage(Component.translatable("minigame.capture.cant-escape", NamedTextColor.RED));
                }
            } else {
                if (team.isInsideBase(capture, player)) {
                    capture.deliveryCarry(player);
                }
            }
        }
    }

    @EventHandler
    public void handleVoidAndFireDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && checkWorld(player.getWorld())) {
            if (capture.isCaptured(player) || capture.isBeingCarried(player) || capture.isEnding()) {
                event.setCancelled(true);
                return;
            }
            DamageCause cause = event.getCause();
            if (cause == DamageCause.VOID) {
                event.setCancelled(true);
                /*
                 * Prevent bugs when a packet of damage by void arrives and
                 * the player was being carried by someone. When the capturer
                 * jumps into void carrying other people, he must pass this
                 * verification to teleport all the carry to the world spawn,
                 * but when the void damage packet arrives and the players have
                 * been already teleported to spawn, it could cause the bug of
                 * it being captured without even tried to jump into void
                 * voluntarily.
                 */
                if (!WorldUtils.belowVoid(player.getLocation())) {
                    return;
                }
                capture.dismountAll(player);
                capture.captured(null, player);
            } else if ((cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK
                    && cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.PROJECTILE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void preventDamageByOthers(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (checkWorld(entity.getWorld())) {
            if (!(event.getDamager() instanceof Player damager) || !(entity instanceof Player victim)) {
                return;
            }
            if (capture.isCaptured(damager) || capture.isCaptured(victim)
                    || capture.isBeingCarried(damager) || capture.isBeingCarried(victim)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDying(PlayerDeathEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Player killer = player.getKiller();
            /*
             * This verification is done to prevent bugs
             * when two players kill each other at the same time
             */
            if (player.equals(capture.getBull(killer).orElse(null))) {
                return;
            }
            capture.dismountAll(player);
            capture.mount(killer, player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player hero = event.getPlayer();
        if (checkWorld(hero.getWorld())) {
            event.setCancelled(true);
            if (event.getRightClicked() instanceof Player princess
                    && CaptureTeam.areSameTeam(capture, hero, princess) && capture.isCaptured(princess)
                    && !capture.isCaptured(hero) && !capture.isBeingCarried(princess)) {
                capture.mount(hero, princess);
            }
        }
    }

    @EventHandler
    public void preventDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (checkWorld(entity.getWorld())) {
            if (entity.getType() == EntityType.ARMOR_STAND) {
                return;
            }
            if (entity instanceof Player player && capture.isBeingCarried(player)) {
                event.setCancelled(true);
                player.sendMessage(Component.translatable("minigame.capture.cant-escape", NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (checkWorld(player.getWorld())) {
            player.teleport(capture.getInfo().lobby());
        }
    }

    @EventHandler
    public void onQuittingMatch(PlayerTeleportEvent event) {
        if (checkWorld(event.getFrom().getWorld()) && !checkWorld(event.getTo().getWorld()) && !capture.isEnding()) {
            capture.onQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void removeArrow(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (checkWorld(projectile.getWorld())) {
            projectile.remove();
        }
    }

    @EventHandler
    public void preserveDurability(PlayerItemDamageEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventDroppingItems(PlayerDropItemEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventPickingUpItems(PlayerPickItemEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventReceiveXP(PlayerPickupExperienceEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            event.getExperienceOrb().remove();
        }
    }

    @EventHandler
    public void preventHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && checkWorld(player.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void preventSpawningEntities(EntitySpawnEvent event) {
        if (checkWorld(event.getLocation().getWorld())) {
            EntityType type = event.getEntityType();
            if (type != EntityType.ARROW && type != EntityType.ARMOR_STAND) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void preventShootingWhileBeingCarried(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player &&
                checkWorld(player.getWorld()) && (capture.isBeingCarried(player) || capture.isCaptured(player))) {
            event.setCancelled(true);
        }
    }

}
