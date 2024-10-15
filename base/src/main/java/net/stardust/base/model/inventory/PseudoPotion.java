package net.stardust.base.model.inventory;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.stardust.base.Stardust;
import net.stardust.base.utils.PseudoObject;
import net.stardust.base.utils.ranges.Ranges;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PseudoPotion extends PseudoItem {

    private HashMap<NamespacedKey, PseudoEffect> customEffects;

    @Setter
    private PotionType type;

    @Setter
    private transient Color color;

    public PseudoPotion() {
        super(Material.POTION);
        customEffects = new HashMap<>();
    }

    public PseudoPotion(PotionType type) {
        this();
        this.type = type;
    }

    public PseudoPotion(Color color) {
        this();
        this.color = color;
    }

    public PseudoPotion(PotionType type, Color color) {
        this();
        this.type = type;
        this.color = color;
    }

    public PseudoPotion(ItemStack potion) {
        super(potion);
        if (!(potion.getItemMeta() instanceof PotionMeta meta)) {
            throw new IllegalArgumentException("not a potion");
        }
        type = meta.getBasePotionType();
        color = meta.getColor();
    }

    private PseudoPotion(HashMap<NamespacedKey, PseudoEffect> customEffects, PotionType type, Color color) {
        super(Material.POTION);
        this.customEffects = customEffects;
        this.type = type;
        this.color = color;
    }

    public void addCustomEffect(NamespacedKey key, PseudoEffect effect) {
        customEffects.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(effect, "effect"));
    }

    public void removeCustomEffect(NamespacedKey key) {
        customEffects.remove(Objects.requireNonNull(key, "key"));
    }

    public boolean containsCustomEffect(NamespacedKey key) {
        return customEffects.containsKey(Objects.requireNonNull(key, "key"));
    }

    public void clearCustomEffects() {
        customEffects.clear();
    }

    public Map<NamespacedKey, PseudoEffect> getCustomEffects() {
        return Collections.unmodifiableMap(customEffects);
    }

    @Override
    public void setMaterial(Material material) {
        throw new UnsupportedOperationException("Potion cannot change material");
    }

    @Override
    public ItemStack toOriginal() {
        ItemStack item = super.toOriginal();

        PotionMeta meta = (PotionMeta) item.getItemMeta();
        customEffects.values().forEach(effect -> meta.addCustomEffect(effect.toOriginal(), true));
        meta.setBasePotionType(type);
        meta.setColor(color);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PseudoPotion clone() {
        return new PseudoPotion((HashMap<NamespacedKey, PseudoEffect>) customEffects.clone(),
                type, Color.fromARGB(color.asARGB()));
    }

    @Serial
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(color.asARGB());
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        color = Color.fromARGB(stream.readInt());
    }

    public record PseudoEffect(
            String effectKey,
            int duration,
            int amplifier,
            boolean ambient,
            boolean particles,
            boolean icon,
            PseudoEffect hiddenEffect
    ) implements PseudoObject<PotionEffect>, Serializable {

        public PseudoEffect {
            Stardust.key(Objects.requireNonNull(effectKey, "effectKey"));
            if (duration == 0 || duration <= -2) {
                duration = 1;
            }
            Ranges.greaterOrEqual(amplifier, 0, "amplifier");
        }

        public PseudoEffect(String effectKey, int duration, int amplifier, boolean ambient,
                            boolean particles, boolean icon) {
            this(effectKey, duration, amplifier, ambient, particles, icon, null);
        }

        public PseudoEffect(String effectKey, int duration, int amplifier, boolean ambient, boolean particles) {
            this(effectKey, duration, amplifier, ambient, particles, true);
        }

        public PseudoEffect(String effectKey, int duration, int amplifier, boolean ambient) {
            this(effectKey, duration, amplifier, ambient, true);
        }

        public PseudoEffect(String effectKey, int duration, int amplifier) {
            this(effectKey, duration, amplifier, false);
        }

        public PseudoEffect(String effectKey, int duration) {
            this(effectKey, duration, 0);
        }

        public PseudoEffect(String effectKey) {
            this(effectKey, 1);
        }

        @Override
        public PotionEffect toOriginal() {
            return new PotionEffect(
                    Registry.EFFECT.get(Stardust.key(effectKey)),
                    duration,
                    amplifier,
                    ambient,
                    particles,
                    icon,
                    hiddenEffect == null ? null : hiddenEffect.toOriginal()
            );
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (o instanceof PseudoEffect effect) {
                return effectKey.equals(effect.effectKey);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return effectKey.hashCode();
        }

        @Override
        public String toString() {
            return "PseudoEffect{" +
                    "effectKey='" + effectKey + '\'' +
                    ", duration=" + duration +
                    ", amplifier=" + amplifier +
                    ", ambient=" + ambient +
                    ", particles=" + particles +
                    ", icon=" + icon +
                    ", hiddenEffect=" + hiddenEffect +
                    '}';
        }

    }

}
