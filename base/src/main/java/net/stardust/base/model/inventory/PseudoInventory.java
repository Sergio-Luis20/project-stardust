package net.stardust.base.model.inventory;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.model.inventory.PseudoInventory.PseudoInventoryDeserializer;
import net.stardust.base.model.inventory.PseudoInventory.PseudoInventorySerializer;
import net.stardust.base.utils.PseudoObject;
import net.stardust.base.utils.inventory.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@ToString
@EqualsAndHashCode
@JsonSerialize(using = PseudoInventorySerializer.class)
@JsonDeserialize(using = PseudoInventoryDeserializer.class)
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

    private PseudoInventory(String titleJson, PseudoItem[] items, Map<String, String> labels) {
        this.titleJson = titleJson;
        this.items = items;
        this.labels = labels;
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

    public String getTitleJson() {
        return titleJson;
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
        return Collections.unmodifiableMap(labels);
    }
    
    public void clear() {
        setItems(null);
    }

    @Override
    public PseudoInventory clone() {
        PseudoInventory copy = new PseudoInventory();
        copy.items = items.clone();
        copy.titleJson = titleJson;
        copy.labels = getLabels();
        return copy;
    }

    private record ItemEntry(int slot, PseudoItem item) {
    }

    private record Items(int size, ItemEntry[] entries) {

        public PseudoItem[] restoreArray() {
            PseudoItem[] items = new PseudoItem[size];
            for (ItemEntry entry : entries) {
                items[entry.slot()] = entry.item();
            }
            return items;
        }

    }

    private Items convertItems() {
        List<ItemEntry> entries = new ArrayList<>();
        int size = items.length;
        for (int i = 0; i < size; i++) {
            PseudoItem item = items[i];
            if (item != null) {
                entries.add(new ItemEntry(i, item));
            }
        }
        return new Items(size, entries.toArray(ItemEntry[]::new));
    }

    static class PseudoInventorySerializer extends JsonSerializer<PseudoInventory> {

        @Override
        public void serialize(PseudoInventory value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("titleJson", value.titleJson);
            gen.writeObjectField("items", value.convertItems());
            gen.writeObjectField("labels", value.labels);
            gen.writeEndObject();
        }

    }

    static class PseudoInventoryDeserializer extends JsonDeserializer<PseudoInventory> {

        @Override
        @SuppressWarnings("unchecked")
        public PseudoInventory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = p.getCodec().readTree(p);

            JsonNode titleNode = node.get("titleJson");
            String titleJson = titleNode instanceof NullNode ? null : titleNode.asText();
            Items items = p.getCodec().treeToValue(node.get("items"), Items.class);
            Map<String, String> labels = p.getCodec().treeToValue(node.get("labels"), HashMap.class);

            return new PseudoInventory(titleJson, items.restoreArray(), labels);
        }

    }

}
