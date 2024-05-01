package net.stardust.base.model.economy.storage;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.stardust.base.utils.inventory.InventoryUtils;

public class InventoryStorage implements Storage {

    private Inventory inventory;

    public InventoryStorage(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
    }

    @Override
    public boolean addItem(ItemStack item) {
        if(!InventoryUtils.canStoreAllItems(inventory, item)) {
            return false;
        }
        inventory.addItem(item);
        return true;
    }

    @Override
    public boolean removeItem(ItemStack item) {
        if(!hasItem(item)) {
            return false;
        }
        if(!InventoryUtils.canRemoveAllItems(inventory, item)) {
            return false;
        }
        inventory.removeItem(item);
        return true;
    }

    @Override
    public boolean hasItem(ItemStack item) {
        for(ItemStack stack : inventory.getStorageContents()) {
            if(stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            if(stack.getType() == item.getType() && Objects.equals(stack.getItemMeta(), item.getItemMeta())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int remainingCapacity() {
        return InventoryUtils.getEmptyStorageSlots(inventory).size();
    }
    
}
