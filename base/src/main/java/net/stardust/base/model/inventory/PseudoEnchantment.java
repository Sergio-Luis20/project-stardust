package net.stardust.base.model.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.*;
import net.stardust.base.utils.PseudoObject;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
public class PseudoEnchantment implements PseudoObject<Enchantment>, Serializable, Cloneable {
    
    @Setter
    @NonNull
    private NamespacedKey namespacedKey;
    private int level;

    public PseudoEnchantment(Enchantment ench, int level) {
        this(ench.getKey(), level);
    }

    @JsonCreator
    public PseudoEnchantment(@JsonProperty(value = "namespacedKey", required = true) NamespacedKey namespacedKey,
                             @JsonProperty(value = "level", required = true) int level) {
        setNamespacedKey(namespacedKey);
        setLevel(level);
    }

    public PseudoEnchantment(PseudoEnchantment ench) {
        namespacedKey = ench.namespacedKey;
        level = ench.level;
    }

    @Override
    public Enchantment toOriginal() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
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
