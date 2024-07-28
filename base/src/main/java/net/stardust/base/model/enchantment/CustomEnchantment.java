package net.stardust.base.model.enchantment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import br.sergio.utils.Pair;
import io.papermc.paper.enchantments.EnchantmentRarity;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.stardust.base.Stardust;
import net.stardust.base.utils.ranges.Ranges;

@NoArgsConstructor
public abstract class CustomEnchantment extends Enchantment {

    private String name, key, translationKey;
    private int maxLevel, startLevel;
    private EnchantmentTarget itemTarget;
    private boolean treasure, cursed, treadable, discoverable;
    private Set<EquipmentSlot> activeSlots;
    private Pair<Integer, Integer> minMod, maxMod;
    private EnchantmentRarity rarity;

    public CustomEnchantment(String name, String key, String translationKey, int maxLevel, int startLevel,
                             EnchantmentTarget itemTarget, boolean treasure, boolean cursed, boolean treadable,
                             boolean discoverable, Set<EquipmentSlot> activeSlots, Pair<Integer, Integer> minMod,
                             Pair<Integer, Integer> maxMod, EnchantmentRarity rarity) {
        builder()
                .name(name)
                .key(key)
                .translationKey(translationKey)
                .maxLevel(maxLevel)
                .startLevel(startLevel)
                .itemTarget(itemTarget)
                .treasure(treasure)
                .cursed(cursed)
                .treadable(treadable)
                .discoverable(discoverable)
                .activeSlots(activeSlots)
                .minMod(minMod)
                .maxMod(maxMod)
                .rarity(rarity)
                .build();
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return startLevel;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return itemTarget;
    }

    @Override
    public boolean isTreasure() {
        return treasure;
    }

    @Override
    public boolean isCursed() {
        return cursed;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    public abstract boolean canEnchantItem(@NotNull ItemStack item);

    @Override
    public @NotNull Component displayName(int level) {
        return Component.translatable(translationKey() + ".display-name", Component.text(level));
    }

    @Override
    public boolean isTradeable() {
        return treadable;
    }

    @Override
    public boolean isDiscoverable() {
        return discoverable;
    }

    @Override
    public int getMinModifiedCost(int level) {
        return minMod.getMale() * level + maxMod.getFemale();
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return maxMod.getMale() * level + maxMod.getFemale();
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return rarity;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return activeSlots;
    }

    @Override
    public @NotNull String translationKey() {
        return "custom-enchant." + translationKey;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return Stardust.stardust(key);
    }

    // Deprecated, should be removed when paper updates
    @Override
    public @NotNull String getTranslationKey() {
        return translationKey();
    }

    protected InnerBuilder builder() {
        return new InnerBuilder();
    }

    protected class InnerBuilder {

        private static final int FIELD_AMOUNT = 14;

        private Set<String> calledMethods;

        private String name, key, translationKey;
        private int maxLevel, startLevel;
        private EnchantmentTarget itemTarget;
        private boolean treasure, cursed, treadable, discoverable;
        private Set<EquipmentSlot> activeSlots;
        private Pair<Integer, Integer> minMod, maxMod;
        private EnchantmentRarity rarity;

        public InnerBuilder() {
            calledMethods = new HashSet<>();
        }

        public InnerBuilder name(String name) {
            this.name = Objects.requireNonNull(name, "name");
            calledMethods.add("name");
            return this;
        }

        public InnerBuilder key(String key) {
            this.key = Objects.requireNonNull(key, "key");
            calledMethods.add("key");
            return this;
        }

        public InnerBuilder translationKey(String translationKey) {
            this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
            calledMethods.add("translationKey");
            return this;
        }

        public InnerBuilder maxLevel(int maxLevel) {
            this.maxLevel = Ranges.greaterOrEqual(maxLevel, 1, "maxLevel");
            calledMethods.add("maxLevel");
            return this;
        }

        public InnerBuilder startLevel(int startLevel) {
            this.startLevel = Ranges.greaterOrEqual(startLevel, 1, "startLevel");
            calledMethods.add("startLevel");
            return this;
        }

        public InnerBuilder itemTarget(EnchantmentTarget itemTarget) {
            this.itemTarget = Objects.requireNonNull(itemTarget, "itemTarget");
            calledMethods.add("itemTarget");
            return this;
        }

        public InnerBuilder treasure(boolean treasure) {
            this.treasure = treasure;
            calledMethods.add("treasure");
            return this;
        }

        public InnerBuilder cursed(boolean cursed) {
            this.cursed = cursed;
            calledMethods.add("cursed");
            return this;
        }

        public InnerBuilder treadable(boolean treadable) {
            this.treadable = treadable;
            calledMethods.add("treadable");
            return this;
        }

        public InnerBuilder discoverable(boolean discoverable) {
            this.discoverable = discoverable;
            calledMethods.add("discoverable");
            return this;
        }

        public InnerBuilder activeSlots(Set<EquipmentSlot> activeSlots) {
            this.activeSlots = Set.copyOf(activeSlots);
            calledMethods.add("activeSlots");
            return this;
        }

        public InnerBuilder activeSlots(EquipmentSlot... activeSlots) {
            return activeSlots(new HashSet<>(Arrays.asList(activeSlots)));
        }

        public InnerBuilder minMod(Pair<Integer, Integer> minMod) {
            this.minMod = Objects.requireNonNull(minMod, "minMod");
            if(minMod.getMale() == null) throw new IllegalArgumentException("null minMod derivative");
            if(minMod.getFemale() == null) throw new IllegalArgumentException("null minMod zero");
            calledMethods.add("minMod");
            return this;
        }

        public InnerBuilder maxMod(Pair<Integer, Integer> maxMod) {
            this.maxMod = Objects.requireNonNull(maxMod, "maxMod");
            if(maxMod.getMale() == null) throw new IllegalArgumentException("null maxMod derivative");
            if(maxMod.getFemale() == null) throw new IllegalArgumentException("null maxMod zero");
            calledMethods.add("maxMod");
            return this;
        }

        public InnerBuilder rarity(EnchantmentRarity rarity) {
            this.rarity = Objects.requireNonNull(rarity, "rarity");
            calledMethods.add("rarity");
            return this;
        }

        public void build() {
            if(calledMethods.size() != FIELD_AMOUNT) {
                throw new IllegalStateException("not all fields are initialized");
            }
            CustomEnchantment.this.name = name;
            CustomEnchantment.this.key = key;
            CustomEnchantment.this.translationKey = translationKey;
            CustomEnchantment.this.maxLevel = maxLevel;
            CustomEnchantment.this.startLevel = startLevel;
            CustomEnchantment.this.itemTarget = itemTarget;
            CustomEnchantment.this.treasure = treasure;
            CustomEnchantment.this.cursed = cursed;
            CustomEnchantment.this.treadable = treadable;
            CustomEnchantment.this.discoverable = discoverable;
            CustomEnchantment.this.activeSlots = activeSlots;
            CustomEnchantment.this.minMod = minMod;
            CustomEnchantment.this.maxMod = maxMod;
            CustomEnchantment.this.rarity = rarity;
        }

    }

    public static Pair<Integer, Integer> modOne() {
        return new Pair<>(0, 1);
    }

}
