package net.stardust.base.model.economy.storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public class ChestStorage implements Storage, Serializable {

    private transient Chest chest;
    private transient Storage child;

    public ChestStorage(Chest chest) {
        this.chest = Objects.requireNonNull(chest, "chest");
        child = new InventoryStorage(chest.getInventory());
    }

    @Override
    public boolean addItem(ItemStack item) {
        return child.addItem(item);
    }

    @Override
    public boolean removeItem(ItemStack item) {
        return child.removeItem(item);
    }

    @Override
    public boolean hasItem(ItemStack item) {
        return child.hasItem(item);
    }

    @Override
    public boolean canStore(List<ItemStack> items) {
        return child.canStore(items);
    }

    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        Location chestLocation = Location.deserialize((Map<String, Object>) input.readObject());
        chest = (Chest) chestLocation.getBlock().getState();
        child = new InventoryStorage(chest.getInventory());
    }

    @Serial
    private void writeObject(ObjectOutputStream output) throws IOException {
        output.defaultWriteObject();
        output.writeObject(chest.getLocation().serialize());
    }

    public Chest getChest() {
        return chest;
    }
    
}
