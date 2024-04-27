package net.stardust.base.utils;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import br.sergio.utils.Pair;
import net.stardust.base.utils.plugin.PluginConfig;

public class ItemUtils {

    public static ItemStack item(Material type) {
        return axe(new ItemStack(type));
    }

    public static ItemStack item(Material type, int amount) {
        return axe(new ItemStack(type, amount));
    }

    public static ItemStack item(ItemStack item) {
        return axe(new ItemStack(item));
    }

    public static ItemStack createHead(String link) {
        return createHead(new ItemStack(Material.PLAYER_HEAD, 1), link);
    }

    public static ItemStack createHead(ItemStack head, String link) {
        if(head.getType() != Material.PLAYER_HEAD) return head;
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        assert meta != null;
        UUID profileId = UUID.randomUUID();
        GameProfile profile = new GameProfile(profileId, profileId.toString());
        profile.getProperties().put("textures", new Property("textures", link));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
            head.setItemMeta(meta);
        } catch(Exception e) {
            PluginConfig.get().getPlugin().getLogger().log(Level.SEVERE
                , "Exceção ao obter cabeça", Throwables.send("ItemUtils/createHead", e));
        }
        return head;
    }
    
    private static ItemStack axe(ItemStack item) {
        if(item == null) return null;
        Material type = item.getType();
        if(!Tag.ITEMS_AXES.isTagged(type)) return item;
        Pair<Double, Double> data = switch(type) {
            case WOODEN_AXE -> new Pair<>(3.0, 0.8);
            case GOLDEN_AXE -> new Pair<>(3.0, 1.0);
            case STONE_AXE -> new Pair<>(4.0, 0.8);
            case IRON_AXE -> new Pair<>(5.0, 0.9);
            case DIAMOND_AXE -> new Pair<>(6.0, 1.0);
            case NETHERITE_AXE -> new Pair<>(7.0, 1.0);
            default -> throw new AssertionError("not an axe");
        };
        return configureAxe(item, data.getMale(), data.getFemale());
    }

    private static ItemStack configureAxe(ItemStack item, double damage, double speed) {
        ItemMeta meta = item.getItemMeta();
        AttributeModifier damageReductor = new AttributeModifier(UUID.randomUUID(), "damage",
                damage, Operation.ADD_NUMBER, EquipmentSlot.HAND);
        AttributeModifier speedPreserver = new AttributeModifier(UUID.randomUUID(), "speed",
                speed, Operation.ADD_NUMBER, EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageReductor);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speedPreserver);
        item.setItemMeta(meta);
        return item;
    }

}
