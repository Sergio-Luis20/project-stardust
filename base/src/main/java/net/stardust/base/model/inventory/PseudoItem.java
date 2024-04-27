package net.stardust.base.model.inventory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.utils.ItemUtils;
import net.stardust.base.utils.PseudoObject;
import net.stardust.base.utils.persistence.DataManager;

@Getter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonRootName("item")
public class PseudoItem implements PseudoObject<ItemStack>, Serializable, Cloneable {
    
    @Setter
    @NonNull
    private Material material;
    private int amount;

    @Setter
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private List<PseudoEnchantment> enchantments = new ArrayList<>();
    private List<PersistentObject> tags = new ArrayList<>();
    private Map<String, String> labels = new HashMap<>();

    public PseudoItem() {
        this(Material.AIR);
    }

    public PseudoItem(Material material) {
        this(material, 1);
    }

    public PseudoItem(Material material, int amount) {
        setMaterial(material);
        setAmount(amount);
    }

    public PseudoItem(ItemStack item) {
        material = item.getType();
        amount = item.getAmount();
        ItemMeta meta;
        try {
            meta = item.getItemMeta();
            if(meta == null) {
                return;
            }
        } catch(NullPointerException e) {
            return;
        }
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        setDisplayName(serializer.serialize(meta.displayName()));
        lore.addAll(meta.lore().stream().map(compoment -> serializer.serialize(compoment)).toList());
        meta.getEnchants().forEach((enchant, level) -> enchantments.add(new PseudoEnchantment(enchant, level)));
        DataManager<ItemMeta> dataManager = new DataManager<>(meta);
        for(NamespacedKey key : dataManager) {
            tags.add(new PersistentObject(key, dataManager.readObject(key)));
        }
    }

    public PseudoItem(PseudoItem item) {
        material = item.material;
        amount = item.amount;
        displayName = item.displayName;
        lore.addAll(item.lore);
        item.enchantments.forEach(ench -> enchantments.add(ench.clone()));
        item.tags.forEach(tag -> tags.add(tag.clone()));
        labels.putAll(item.labels);
    }

    public ItemStack toOriginal() {
        ItemStack item = ItemUtils.item(material, amount);
        ItemMeta meta;
        try {
            meta = item.getItemMeta();
        } catch(NullPointerException e) {
            return item;
        }
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        if(displayName != null && !displayName.isEmpty()) meta.displayName(serializer.deserialize(displayName));
        meta.lore(lore.stream().map(line -> (Component) serializer.deserialize(line)).toList());
        enchantments.forEach(ench -> meta.addEnchant(ench.toOriginal(), ench.getLevel(), ench.isCustom()));
        DataManager<ItemMeta> dataManager = new DataManager<>(meta);
        tags.forEach(tag -> dataManager.writeObject(tag.getNamespacedKey(), tag.getValue()));
        item.setItemMeta(meta);
        return item;
    }

    public static PseudoItem[] toPseudoItem(ItemStack[] array) {
        if(array == null) return null;
        PseudoItem[] pseudoArray = new PseudoItem[array.length];
        for(int i = 0; i < array.length; i++) {
            pseudoArray[i] = array[i] == null ? null : new PseudoItem(array[i]);
        }
        return pseudoArray;
    }

    public static ItemStack[] toItemStack(PseudoItem[] array) {
        if(array == null) return null;
        ItemStack[] itemArray = new ItemStack[array.length];
        for(int i = 0; i < array.length; i++) {
            itemArray[i] = array[i] == null ? null : array[i].toOriginal();
        }
        return itemArray;
    }

    @Override
    public PseudoItem clone() {
        return new PseudoItem(this);
    }

    public void setAmount(int amount) {
        this.amount = amount <= 0 ? 1 : amount;
    }

}
