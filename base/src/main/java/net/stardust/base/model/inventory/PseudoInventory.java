package net.stardust.base.model.inventory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.utils.PseudoObject;

@JsonRootName("inventory")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
public class PseudoInventory implements PseudoObject<Inventory>, Serializable, Cloneable {

    public static final int DEFAULT_INVENTORY_SIZE = 54;
    
    @Getter
    private int size;

    @Getter
    @Setter
    private String title;
    
    private Map<Integer, PseudoItem> items = new HashMap<>();

    public PseudoInventory() {
        this(DEFAULT_INVENTORY_SIZE);
    }

    public PseudoInventory(int size) {
        this(size, null);
    }

    public PseudoInventory(int size, String title) {
        setSize(size);
        setTitle(title);
    }

    public PseudoInventory(Inventory inventory) {
        this(inventory, null);
    }

    public PseudoInventory(Inventory inventory, String title) {
        setSize(inventory.getSize());
        setTitle(title);
        for(int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            if(item != null) {
                items.put(i, new PseudoItem(item));
            }
        }
    }

    public PseudoInventory(PseudoInventory inventory) {
        size = inventory.size;
        title = inventory.title;
        inventory.items.forEach((slot, item) -> items.put(slot, item.clone()));
    }

    public PseudoItem[] getPseudoContents() {
        PseudoItem[] pseudoContents = new PseudoItem[size];
        items.forEach((slot, item) -> pseudoContents[slot] = item);
        return pseudoContents;
    }

    public ItemStack[] getContents() {
        return PseudoItem.toItemStack(getPseudoContents());
    }

    @Override
    public Inventory toOriginal() {
        return toInventory(title);
    }

    public Inventory toInventory(String newTitle) {
        return toInventory(null, newTitle);
    }
    
    public Inventory toInventory(InventoryHolder holder) {
    	return toInventory(holder, title);
    }
    
    public Inventory toInventory(InventoryHolder holder, String newTitle) {
    	LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        Inventory inventory = Bukkit.createInventory(holder, size, serializer.deserialize(newTitle));
        inventory.setContents(getContents());
        return inventory;
    }

    public InventoryView openTo(Player player) {
        return player.openInventory(toOriginal());
    }

    public void setSize(int size) {
        int old = this.size;
        this.size = size <= 0 || size % 9 != 0 ? DEFAULT_INVENTORY_SIZE : size;
        if(this.size < old) {
            Iterator<Integer> keyIterator = items.keySet().iterator();
            while(keyIterator.hasNext()) {
                if(keyIterator.next() >= this.size) keyIterator.remove();
            }
        }
    }

    public PseudoItem getItem(int slot) {
        return items.get(slot);
    }

    public PseudoItem setItem(int slot, PseudoItem item) {
        if(slot < 0 || slot >= size) {
            throw new IndexOutOfBoundsException("slot: " + slot + ", inventory size: " + size);
        }
        return items.put(slot, item);
    }
    
    public PseudoItem removeItem(int slot) {
        return items.remove(slot);
    }

    public Map<Integer, PseudoItem> getReadOnlyItems() {
        return Collections.unmodifiableMap(items);
    }

    public void setItems(Map<Integer, PseudoItem> items) {
        if(items == null) {
            this.items.clear();
            return;
        }
        Map<Integer, PseudoItem> newItems = new HashMap<>();
        Iterator<Entry<Integer, PseudoItem>> iterator = items.entrySet().iterator();
        int biggestSlot = 0;
        while(iterator.hasNext()) {
            Entry<Integer, PseudoItem> entry = iterator.next();
            PseudoItem item = entry.getValue();
            if(item == null) continue;
            int slot = entry.getKey();
            if(slot < 0) throw new IndexOutOfBoundsException("negative slot: " + slot);
            if(slot > biggestSlot) biggestSlot = slot;
            newItems.put(slot, item);
        }
        if(biggestSlot >= size) {
            // find next multiple of 9 to expand the inventory
            int sumOne = biggestSlot + 1;
            size = sumOne + 9 - sumOne % 9;
        }
        this.items = newItems;
    }

    @Override
    public PseudoInventory clone() {
        return new PseudoInventory(this);
    }

}
