package net.stardust.base.utils.item;

import net.stardust.base.utils.ranges.Ranges;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public record Enchant(Enchantment enchantment, int level, boolean ignoreLevelRestrictions) {

    public Enchant {
        Objects.requireNonNull(enchantment, "enchantment");
        Ranges.greater(level, 0, "level");
    }

    public Enchant(Enchantment enchantment) {
        this(enchantment, 1, true);
    }

    public Enchant(Enchantment enchantment, int level) {
        this(enchantment, level, true);
    }

    public void addTo(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        addTo(meta);
        item.setItemMeta(meta);
    }

    public void addTo(ItemMeta meta) {
        meta.addEnchant(enchantment, level, ignoreLevelRestrictions);
    }

}
