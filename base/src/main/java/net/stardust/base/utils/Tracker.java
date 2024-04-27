package net.stardust.base.utils;

import br.sergio.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.utils.persistence.DataManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tracker {

    private static final NamespacedKey TRACKER_KEY = new NamespacedKey("stardust", "tracker");
    private static final ItemStack tracker;

    public static Listener newTrackListener(World world) {
        return newTrackListener(world, Integer.MAX_VALUE);
    }

    public static Listener newTrackListener(World world, int maxTrackDistance) {
        return new TrackListener(world, maxTrackDistance);
    }

    public static void giveTo(List<Player> players) {
        players.forEach(Tracker::giveTo);
    }

    public static void giveTo(Player player) {
        player.getInventory().addItem(tracker);
    }

    public static void removeFrom(List<Player> players) {
        players.forEach(Tracker::removeFrom);
    }

    public static void removeFrom(Player player) {
        Inventory inventory = player.getInventory();
        int size = inventory.getSize();
        for(int i = 0; i < size; i++) {
            if(isTracker(inventory.getItem(i))) {
                inventory.setItem(i, null);
            }
        }
    }

    public static boolean isTracker(ItemStack item) {
        if(item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        return manager.readBoolean(TRACKER_KEY);
    }

    private static class TrackListener implements Listener {

        private World world;
        private int maxTrackDistance;

        public TrackListener(World world, int maxTrackDistance) {
            if(maxTrackDistance < 0) {
                throw new IllegalArgumentException("maxTrackDistance must be positive");
            }
            this.world = Objects.requireNonNull(world, "world");
            this.maxTrackDistance = maxTrackDistance;
        }

        @EventHandler
        public void track(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            if(!player.getWorld().equals(world)) {
                return;
            }
            Action action = event.getAction();
            if(action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
                return;
            }
            if(isTracker(event.getItem())) {
                Location loc = player.getLocation();
                List<Player> players = world.getPlayers().stream().filter(p ->
                        p.getLocation().distance(loc) <= maxTrackDistance).collect(Collectors.toList());
                players.remove(player);
                Pair<Player, Double> tracked = new Pair<>(null, Double.POSITIVE_INFINITY);
                for(Player p : players) {
                    double distance = p.getLocation().distance(loc);
                    if(distance < tracked.getFemale()) {
                        tracked.setMale(p);
                        tracked.setFemale(distance);
                    }
                }
                player.sendMessage(getMessage(tracked));
            }
        }

        private static Component getMessage(Pair<Player, Double> tracked) {
            Player trackedPlayer = tracked.getMale();
            Component trackedName;
            if(tracked.getMale() == null) {
                trackedName = Component.text("null", NamedTextColor.GRAY);
            } else {
                trackedName = Component.text(trackedPlayer.getName(), NamedTextColor.GRAY);
            }
            Component trackedDistance = Component.text(tracked.getFemale(), NamedTextColor.GRAY);
            return Component.translatable("track", NamedTextColor.GRAY, trackedName, trackedDistance);
        }

    }

    static {
        tracker = new ItemStack(Material.COMPASS);
        ItemMeta meta = tracker.getItemMeta();
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        manager.writeObject(TRACKER_KEY, true);
        tracker.setItemMeta(meta);
    }

}
