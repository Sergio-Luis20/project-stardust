package net.stardust.base.model.inventory;

import java.io.Serializable;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.stardust.base.utils.PseudoObject;

@Getter
@EqualsAndHashCode
@JsonRootName("enchantment")
public class PseudoEnchantment implements PseudoObject<Enchantment>, Serializable, Cloneable {
    
    @Setter
    @NonNull
    private NamespacedKey namespacedKey;
    private int level;

    public PseudoEnchantment(Enchantment ench, int level) {
        this(ench.getKey(), level);
    }

    public PseudoEnchantment(@JsonProperty("namespacedKey") NamespacedKey namespacedKey, @JsonProperty("level") int level) {
        setNamespacedKey(namespacedKey);
        setLevel(level);
    }

    public PseudoEnchantment(PseudoEnchantment ench) {
        namespacedKey = ench.namespacedKey;
        level = ench.level;
    }

    public Enchantment toOriginal() {
        return Registry.ENCHANTMENT.get(namespacedKey);
    }

    public void setLevel(int level) {
        this.level = level <= 0 ? 1 : level;
    }

    @JsonIgnore
    public boolean isCustom() {
        return !namespacedKey.getNamespace().equals(NamespacedKey.MINECRAFT);
    }

    @Override
    public PseudoEnchantment clone() {
        return new PseudoEnchantment(this);
    }

}
