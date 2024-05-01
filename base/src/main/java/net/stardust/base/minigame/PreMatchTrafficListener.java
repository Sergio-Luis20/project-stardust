package net.stardust.base.minigame;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.inventory.CantStoreItemsException;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

@Getter
public class PreMatchTrafficListener implements Listener {

    private Minigame parent;

    public PreMatchTrafficListener(Minigame parent) {
        this.parent = Objects.requireNonNull(parent, "parent");
    }

    @EventHandler
    public void enterOrLeaveMatchByTeleport(PlayerTeleportEvent event) {
        World world = parent.getWorld();
        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();
        BossBar bar = parent.getPreMatchBar();
        Player player = event.getPlayer();
        if(!from.equals(world) && to.equals(world)) {
            parent.getSnapshot().takeSnapshot(player);
            if(parent.getShop() != null) {
                try {
                    MinigameShop.giveShopBook(player);
                } catch(CantStoreItemsException e) {
                    Throwables.sendAndThrow(e);
                }
            }
            bar.addViewer(player);
        } else if(from.equals(world) && !to.equals(world)) {
            bar.removeViewer(player);
        }
        /*
         * Any other teleport type will be between the minigame world
         * or any other worlds, so we can ignore them.
         */
    }

    @EventHandler
    public void quitMatchByDisconnect(PlayerQuitEvent event) {
        parent.getPreMatchBar().removeViewer(event.getPlayer());
    }

}
