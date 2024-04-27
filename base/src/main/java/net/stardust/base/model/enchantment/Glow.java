package net.stardust.base.model.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Glow extends CustomEnchantment {

    public Glow() {
        builder()
                .name("Glow")
                .key("glow")
                .translationKey("glow")
                .maxLevel(1)
                .startLevel(1)
                .itemTarget(EnchantmentTarget.valueOf("ALL")) // avoid deprecation warning
                .treasure(false)
                .cursed(false)
                .treadable(false)
                .discoverable(false)
                .activeSlots(EquipmentSlot.values())
                .minMod(CustomEnchantment.modOne())
                .maxMod(CustomEnchantment.modOne())
                .rarity(EnchantmentRarity.COMMON)
                .build();
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return true;
    }

}
