package net.stardust.base.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryUtils {
    
    private InventoryUtils() {}

    public static List<Integer> getEmptyStorageSlots(Inventory inventory) {
        List<Integer> emptySlots = new ArrayList<>();
        ItemStack[] storageContents = inventory.getStorageContents();
        for(int i = 0; i < storageContents.length; i++) {
            if(storageContents[i] == null || storageContents[i].getType() == Material.AIR) {
                emptySlots.add(i);
            }
        }
        return emptySlots;
    }

    public static Map<ItemStack, Integer> itemCount(List<ItemStack> items) {
        Map<ItemStack, Integer> count = new HashMap<>();
        items.forEach(item -> count.put(item, count.getOrDefault(item, 0) + 1));
        return count;
    }

    public static boolean canStoreAllItems(Inventory inventory, ItemStack... items) {
        return copyStorageInventory(inventory).addItem(items).isEmpty();
    }

    public static boolean canRemoveAllItems(Inventory inventory, ItemStack... items) {
        return copyStorageInventory(inventory).removeItem(items).isEmpty();
    }

    public static Inventory copyStorageInventory(Inventory inventory) {
        int size = inventory instanceof PlayerInventory ? 36 : inventory.getSize();
        Inventory copy = Bukkit.createInventory(null, size);
        copy.setStorageContents(inventory.getStorageContents());
        return copy;
    }

}
