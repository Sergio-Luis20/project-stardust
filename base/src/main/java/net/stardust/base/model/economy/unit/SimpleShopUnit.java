package net.stardust.base.model.economy.unit;

import java.util.Objects;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.storage.Storage;

public class SimpleShopUnit implements ShopUnit {
    
    private Storage storage;
    private ItemStack item;

    public SimpleShopUnit(Storage storage, ItemStack item) {
        this.storage = Objects.requireNonNull(storage, "storage");
        this.item = Objects.requireNonNull(item, "item");
    }

    public Storage getStorage() {
        return storage;
    }

    public ItemStack getItem() {
        return item;
    }

}
