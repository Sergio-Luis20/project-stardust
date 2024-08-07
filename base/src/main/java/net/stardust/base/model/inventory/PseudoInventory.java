package net.stardust.base.model.inventory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.utils.PseudoObject;
import net.stardust.base.utils.inventory.InventoryUtils;

@JsonRootName("inventory")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode
@ToString
public class PseudoInventory implements PseudoObject<Inventory>, Serializable, Cloneable {

    private String titleJson;
    private PseudoItem[] items;
    private Map<String, String> labels;

    public PseudoInventory() {
        this(InventoryUtils.DEFAULT_INVENTORY_SIZE);
    }

    public PseudoInventory(int size) {
        this(size, (Component) null);
    }

    public PseudoInventory(int size, String title) {
        items = new PseudoItem[InventoryUtils.checkSize(size)];
        labels = new HashMap<>();
        setTitle(title);
    }

    public PseudoInventory(int size, Component title) {
        items = new PseudoItem[InventoryUtils.checkSize(size)];
        labels = new HashMap<>();
        setTitle(title);
    }

    public PseudoInventory(Inventory inventory) {
        this(inventory, (Component) null);
    }

    public PseudoInventory(Inventory inventory, String title) {
        this(inventory.getSize(), title);
        setupItems(inventory);
    }

    public PseudoInventory(Inventory inventory, Component title) {
        this(inventory.getSize(), title);
        setupItems(inventory);
    }
    
    public PseudoInventory(PseudoInventory inventory) {
        items = new PseudoItem[inventory.getSize()];
        labels = new HashMap<>();
        this.titleJson = inventory.titleJson; // avoid unnecessary serialization and deserialization

        int size = getSize();
        for (int i = 0; i < size; i++) {
            PseudoItem item = inventory.getItem(i);
            items[i] = item == null ? null : item.clone();
        }
    }

    private void setupItems(Inventory inventory) {
        int size = getSize();
        for (int i = 0; i < size; i++) {
            ItemStack item = inventory.getItem(i);
            items[i] = item == null ? null : new PseudoItem(item);
        }
    }

    public ItemStack[] getContents() {
        return PseudoItem.toItemStack(getItems());
    }

    @Override
    public Inventory toOriginal() {
        return toInventory(null);
    }
    
    public Inventory toInventory(InventoryHolder holder) {
        Inventory inventory = Bukkit.createInventory(holder, getSize(), getTitleAsComponent());
        inventory.setContents(getContents());
        return inventory;
    }

    public InventoryView openTo(Player player) {
        return openTo(player, null);
    }

    public InventoryView openTo(Player player, InventoryHolder holder) {
        return player.openInventory(toInventory(holder));
    }

    public int getSize() {
        return items.length;
    }

    public void setSize(int size) {
        if (size == getSize()) {
            return;
        }
        if (size <= 0 || size > 54 || size % 9 != 0) {
            throw new IllegalArgumentException("size must be a non zero multiple of 9 less or equal to 54");
        }
        this.items = Arrays.copyOf(items, size);
    }

    public PseudoItem getItem(int slot) {
        return items[slot];
    }

    public PseudoItem setItem(int slot, PseudoItem item) {
        PseudoItem old = items[slot];
        items[slot] = item;
        return old;
    }
    
    public PseudoItem removeItem(int slot) {
        return setItem(slot, null);
    }

    public PseudoItem[] getItems() {
        return items.clone();
    }

    public void setItems(PseudoItem[] items) {
        if (items == null) {
            this.items = new PseudoItem[getSize()];
            return;
        }
        InventoryUtils.checkSize(items.length);
        this.items = items;
    }

    public String getTitleAsString() {
        if (titleJson == null) {
            return null;
        }
        Component component = JSONComponentSerializer.json().deserialize(titleJson);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public Component getTitleAsComponent() {
        return titleJson == null ? null : JSONComponentSerializer.json().deserialize(titleJson);
    }

    public void setTitle(String title) {
        if (title == null) {
            this.titleJson = null;
            return;
        }
        setTitle(LegacyComponentSerializer.legacySection().deserialize(title));
    }

    public void setTitle(Component title) {
        if (title == null) {
            this.titleJson = null;
            return;
        }
        this.titleJson = JSONComponentSerializer.json().serialize(title);
    }

    public String getLabel(String key) {
        return labels.get(key);
    }

    public String setLabel(String key, String value) {
        if (key == null) {
            return null;
        }
        return value == null ? labels.remove(key) : labels.put(key, value);
    }

    public boolean containsLabel(String key) {
        return labels.containsKey(key);
    }

    public String removeLabel(String key) {
        return setLabel(key, null);
    }

    public Map<String, String> getLabels() {
        Map<String, String> copy = new HashMap<>();
        copy.putAll(labels);
        return copy;
    }
    
    public void clear() {
        setItems(null);
    }

    @Override
    public PseudoInventory clone() {
        return new PseudoInventory(this);
    }

}
