package net.stardust.base.utils.gameplay;

import net.stardust.base.utils.persistence.DataManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Tracker {

    private static final NamespacedKey TRACKER_KEY = new NamespacedKey("stardust", "tracker");
    private static final ItemStack tracker;

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

    static {
        tracker = new ItemStack(Material.COMPASS);
        ItemMeta meta = tracker.getItemMeta();
        DataManager<ItemMeta> manager = new DataManager<>(meta);
        manager.writeObject(TRACKER_KEY, true);
        tracker.setItemMeta(meta);
    }

}
