package net.stardust.base.nms;

import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentSlotType;
import net.stardust.base.model.enchantment.CustomEnchantment;

// Do not use this class yet
public class NmsEnchantment extends Enchantment {

    protected NmsEnchantment(CustomEnchantment customEnchantment) {
        super(Rarity.valueOf(customEnchantment.getRarity().name()), EnchantmentSlotType.valueOf(""),
                EnumItemSlot.values());
    }

}
