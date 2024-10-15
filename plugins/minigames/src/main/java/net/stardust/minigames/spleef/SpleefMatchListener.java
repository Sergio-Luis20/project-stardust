package net.stardust.minigames.spleef;

import net.stardust.base.events.WorldListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;

public class SpleefMatchListener extends WorldListener {

    private Spleef spleef;

    public SpleefMatchListener(Spleef spleef) {
        super(spleef::getWorld);
        this.spleef = spleef;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (checkWorld(event.getFrom().getWorld()) && !checkWorld(event.getTo().getWorld())) {
            spleef.onQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (checkWorld(player.getWorld())) {
            player.teleport(spleef.getInfo().lobby());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (checkWorld(event.getPlayer().getWorld()) && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.getClickedBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        World world = projectile.getWorld();
        if (checkWorld(world)) {
            event.setCancelled(true);
            Block block = event.getHitBlock();
            if (block != null) {
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        world.getBlockAt(x + i, y, z + j).setType(Material.AIR);
                    }
                }
            }
            projectile.remove();
        }
    }

    @EventHandler
    public void onFalling(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (checkWorld(player.getWorld()) && event.getTo().getY() < spleef.getFallLimit()) {
            spleef.exit(player, true, true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (checkWorld(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if (checkWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

}
