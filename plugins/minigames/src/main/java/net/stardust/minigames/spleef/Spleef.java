package net.stardust.minigames.spleef;

import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.MinigameShop;
import net.stardust.base.utils.inventory.InventoryUtils;
import net.stardust.base.utils.item.Effect;
import net.stardust.base.utils.item.Enchant;
import net.stardust.base.utils.item.ItemUtils;
import net.stardust.base.utils.item.PotionType;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

import static net.stardust.base.minigame.MinigameShop.createShopItem;
import static net.stardust.base.utils.item.ItemUtils.potion;

public class Spleef extends Minigame {

    private MinigamesPlugin plugin;
    private BukkitRunnable healthDecreasingTask;
    private List<Player> players;
    private List<Player> losers;

    @Getter(AccessLevel.PACKAGE)
    private int fallLimit;

    public Spleef(MinigamesPlugin plugin, int index) {
        super(plugin.getMinigameInfo("spleef", index));
        this.plugin = plugin;
        setMatchListener(new SpleefMatchListener(this));
        setShop(MinigameShop.newShop(this, false,
                Component.translatable("word.shop"), InventoryUtils.DEFAULT_INVENTORY_SIZE,
                getShopItems()));
    }

    @Override
    protected void match() {
        setupFallLimit();
        players = new ArrayList<>(getWorld().getPlayers());
        losers = new ArrayList<>();
        healthDecreasingTask = new BukkitRunnable() {

            @Override
            public void run() {
                Iterator<Player> iterator = players.iterator();
                while (iterator.hasNext()) {
                    Player player = iterator.next();
                    double health = player.getHealth();
                    if (health < 1) {
                        iterator.remove();
                        exit(player, true, false);
                    } else {
                        player.setHealth(health - 1);
                    }
                }
            }

        };
        healthDecreasingTask.runTaskTimer(plugin, 0, 30); // 1.5 s
    }

    @Override
    protected void onMatchInterrupted() {
        healthDecreasingTask.cancel();
    }

    void onQuit(Player player) {
        players.remove(player);
        exit(player, false, false);
    }

    void checkVictory() {
        switch (players.size()) {
            case 0, 1 -> {
                healthDecreasingTask.cancel();
                endMatch(players, losers);
            }
        }
    }

    void exit(Player player, boolean lost, boolean remove) {
        if (lost) {
            losers.add(player);
            player.teleport(getInfo().lobby());
        }
        if (remove) {
            players.remove(player);
        }
        getSnapshot().restore(player);
        checkVictory();
    }

    private void setupFallLimit() {
        File mapConfigurationFile = new File(getWorld().getWorldFolder(), "spleef.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(mapConfigurationFile);
        fallLimit = configuration.getInt("fall-limit", -64);
    }

    private Map<Integer, ItemStack> getShopItems() {
        Map<Integer, ItemStack> items = new HashMap<>();

        items.put(0, createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.INFINITY)
        ), 4));
        items.put(1, createShopItem(new ItemStack(Material.ARROW), 1));
        items.put(9, createShopItem(new ItemStack(Material.MILK_BUCKET), 2));

        items.put(18, createShopItem(potion(PotionType.NORMAL, Effect.normal(PotionEffectType.JUMP_BOOST)), 3));
        items.put(19, createShopItem(potion(PotionType.NORMAL, Effect.extended(PotionEffectType.JUMP_BOOST)), 4));
        items.put(20, createShopItem(potion(PotionType.NORMAL, Effect.amplified(PotionEffectType.JUMP_BOOST)), 5));
        items.put(21, createShopItem(potion(PotionType.SPLASH, Effect.normal(PotionEffectType.JUMP_BOOST)), 6));
        items.put(22, createShopItem(potion(PotionType.SPLASH, Effect.extended(PotionEffectType.JUMP_BOOST)), 7));
        items.put(23, createShopItem(potion(PotionType.SPLASH, Effect.amplified(PotionEffectType.JUMP_BOOST)), 8));
        items.put(24, createShopItem(potion(PotionType.LINGERING, Effect.normal(PotionEffectType.JUMP_BOOST)), 9));
        items.put(25, createShopItem(potion(PotionType.LINGERING, Effect.extended(PotionEffectType.JUMP_BOOST)), 10));
        items.put(26, createShopItem(potion(PotionType.LINGERING, Effect.amplified(PotionEffectType.JUMP_BOOST)), 11));

        items.put(27, createShopItem(potion(PotionType.NORMAL, Effect.normal(PotionEffectType.SPEED)), 3));
        items.put(28, createShopItem(potion(PotionType.NORMAL, Effect.extended(PotionEffectType.SPEED)), 4));
        items.put(29, createShopItem(potion(PotionType.NORMAL, Effect.amplified(PotionEffectType.SPEED)), 5));
        items.put(30, createShopItem(potion(PotionType.SPLASH, Effect.normal(PotionEffectType.SPEED)), 6));
        items.put(31, createShopItem(potion(PotionType.SPLASH, Effect.extended(PotionEffectType.SPEED)), 7));
        items.put(32, createShopItem(potion(PotionType.SPLASH, Effect.amplified(PotionEffectType.SPEED)), 8));
        items.put(33, createShopItem(potion(PotionType.LINGERING, Effect.normal(PotionEffectType.SPEED)), 9));
        items.put(34, createShopItem(potion(PotionType.LINGERING, Effect.extended(PotionEffectType.SPEED)), 10));
        items.put(35, createShopItem(potion(PotionType.LINGERING, Effect.amplified(PotionEffectType.SPEED)), 11));

        items.put(36, createShopItem(potion(PotionType.NORMAL, Effect.normal(PotionEffectType.REGENERATION)), 3));
        items.put(37, createShopItem(potion(PotionType.NORMAL, Effect.extended(PotionEffectType.REGENERATION)), 4));
        items.put(38, createShopItem(potion(PotionType.NORMAL, Effect.amplified(PotionEffectType.REGENERATION)), 5));
        items.put(39, createShopItem(potion(PotionType.SPLASH, Effect.normal(PotionEffectType.REGENERATION)), 6));
        items.put(40, createShopItem(potion(PotionType.SPLASH, Effect.extended(PotionEffectType.REGENERATION)), 7));
        items.put(41, createShopItem(potion(PotionType.SPLASH, Effect.amplified(PotionEffectType.REGENERATION)), 8));
        items.put(42, createShopItem(potion(PotionType.LINGERING, Effect.normal(PotionEffectType.REGENERATION)), 9));
        items.put(43, createShopItem(potion(PotionType.LINGERING, Effect.extended(PotionEffectType.REGENERATION)), 10));
        items.put(44, createShopItem(potion(PotionType.LINGERING, Effect.amplified(PotionEffectType.REGENERATION)), 11));

        items.put(45, createShopItem(potion(PotionType.NORMAL, Effect.instant(PotionEffectType.INSTANT_HEALTH, 1)), 3));
        items.put(46, createShopItem(potion(PotionType.NORMAL, Effect.instant(PotionEffectType.INSTANT_HEALTH, 2)), 5));
        items.put(47, createShopItem(potion(PotionType.SPLASH, Effect.instant(PotionEffectType.INSTANT_HEALTH, 1)), 6));
        items.put(48, createShopItem(potion(PotionType.SPLASH, Effect.instant(PotionEffectType.INSTANT_HEALTH, 2)), 8));
        items.put(49, createShopItem(potion(PotionType.LINGERING, Effect.instant(PotionEffectType.INSTANT_HEALTH, 1)), 9));
        items.put(50, createShopItem(potion(PotionType.LINGERING, Effect.instant(PotionEffectType.INSTANT_HEALTH, 2)), 11));

        return items;
    }

}
