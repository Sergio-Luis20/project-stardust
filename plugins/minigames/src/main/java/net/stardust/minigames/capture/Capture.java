package net.stardust.minigames.capture;

import br.sergio.utils.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.Stardust;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.MinigameShop;
import net.stardust.base.minigame.SquadChannel;
import net.stardust.base.minigame.SquadMinigame;
import net.stardust.base.utils.BossBarUtils;
import net.stardust.base.utils.inventory.InventoryUtils;
import net.stardust.base.utils.SoundPack;
import net.stardust.base.utils.database.lang.Translation;
import net.stardust.base.utils.item.Enchant;
import net.stardust.base.utils.item.ItemUtils;
import net.stardust.base.utils.plugin.PluginConfig;
import net.stardust.base.utils.world.WorldUtils;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class Capture extends Minigame implements SquadMinigame {

    private static final List<PotionEffect> CAPTURED_EFFECTS = List.of(
            new PotionEffect(PotionEffectType.BLINDNESS, 100000, 3, false, false, true),
            new PotionEffect(PotionEffectType.SLOW, 100000, 2, false, false, true)
    );

    private static final SoundPack BEING_CARRIED = new SoundPack(Sound.ENTITY_IRON_GOLEM_HURT, 2f);
    private static final SoundPack CARRYING = new SoundPack(Sound.ENTITY_PLAYER_LEVELUP, 2f);
    private static final SoundPack BASE_DELIVERY = new SoundPack(Sound.ENTITY_PLAYER_LEVELUP, 0.5f);

    private static final int CARRY_TIME = 60;

    private MinigamesPlugin plugin;

    @Getter(AccessLevel.PACKAGE)
    private List<Player> blueTeam, redTeam;
    private Location blueBase, redBase, spawn;
    private SquadChannel blueChannel, redChannel;
    private BossBar time;

    private Scoreboard scoreboard;
    private Objective playerDistribution;

    @Getter(AccessLevel.PACKAGE)
    private Score police, thief;

    private Map<Player, Pair<Integer, Integer>> stats;
    private Map<Player, List<Player>> carry;
    private Map<Player, CarryTimer> timers;

    @Getter(AccessLevel.PACKAGE)
    private List<Player> blueFree, blueCaptured, redFree, redCaptured;

    public Capture(MinigamesPlugin plugin, int index) {
        super(plugin.getMinigameInfo("capture", index));
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        blueTeam = new ArrayList<>();
        redTeam = new ArrayList<>();
        time = BossBarUtils.newDefaultBar();
        stats = new HashMap<>();
        carry = new HashMap<>();
        timers = new HashMap<>();

        blueFree = new ArrayList<>();
        blueCaptured = new ArrayList<>();
        redFree = new ArrayList<>();
        redCaptured = new ArrayList<>();

        setMatchListener(new CaptureMatchListener(this));
        setShop(MinigameShop.newShop(this, false, Component.translatable("word.shop"),
                InventoryUtils.DEFAULT_INVENTORY_SIZE, getShopItems()));
    }

    @Override
    protected void match() {
        distributePlayers();
        setupBasesAndSpawn();
        setupSquadChannels();
        setupScoreboard();
        equipPlayers();
        teleportPlayers();
    }

    @Override
    protected void onSpawnCommand(Player player) {
        time.removeViewer(player);
        getSnapshot().restore(player);
        player.teleport(getInfo().lobby());
    }

    private void distributePlayers() {
        World world = getWorld();
        List<Player> players = world.getPlayers();
        Collections.shuffle(players);
        CaptureTeam team = CaptureTeam.randomTeam();
        for(Player player : players) {
            stats.put(player, new Pair<>(0, 0));
            carry.put(player, new ArrayList<>());
            team.getPlayers(this).add(player);
            team = team.other();
        }
        blueFree.addAll(blueTeam);
        redFree.addAll(redTeam);
    }

    private void setupBasesAndSpawn() {
        File mapConfigurationFile = new File(getWorld().getWorldFolder(), "capture.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(mapConfigurationFile);
        blueBase = getLocation(configuration.getConfigurationSection("blue-base"));
        redBase = getLocation(configuration.getConfigurationSection("red-base"));
        spawn = getLocation(configuration.getConfigurationSection("spawn"));
    }

    private void setupSquadChannels() {
        blueChannel = new SquadChannel(plugin, "Capture Blue Squad Channel", blueTeam,
                (sender, message) -> formatTeamMessage(sender, message, NamedTextColor.BLUE, "blue"));
        redChannel = new SquadChannel(plugin, "Capture Red Squad Channel", redTeam,
                (sender, message) -> formatTeamMessage(sender, message, NamedTextColor.RED, "red"));
    }

    private void setupScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        playerDistribution = scoreboard.registerNewObjective("Players", Criteria.DUMMY,
                Component.text("Players", NamedTextColor.GOLD, TextDecoration.BOLD));
        playerDistribution.setDisplaySlot(DisplaySlot.SIDEBAR);

        configureTeam("Blue", blueTeam, NamedTextColor.BLUE);
        configureTeam("Red", redTeam, NamedTextColor.RED);

        police = configureScore("Police", NamedTextColor.BLUE, blueFree);
        thief = configureScore("Thief", NamedTextColor.RED, redFree);
    }

    private void equipPlayers() {
        equipTeam(blueTeam, Color.BLUE);
        equipTeam(redTeam, Color.RED);
    }

    private void teleportPlayers() {
        blueTeam.forEach(player -> player.teleport(blueBase));
        redTeam.forEach(player -> player.teleport(redBase));
    }

    private void equipTeam(List<Player> team, Color color) {
        for(Player player : team) {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armor = {
                    new ItemStack(Material.LEATHER_HELMET),
                    new ItemStack(Material.LEATHER_CHESTPLATE),
                    new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_BOOTS)
            };
            configureArmor(color, armor);
            inventory.setArmorContents(armor);

            if(inventory.first(Material.WOODEN_SWORD) == -1) {
                inventory.addItem(new ItemStack(Material.WOODEN_SWORD));
            }
            if(inventory.first(Material.BOW) == -1) {
                ItemStack bow = new ItemStack(Material.BOW);
                ItemMeta meta = bow.getItemMeta();
                meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
                bow.setItemMeta(meta);
                inventory.addItem(bow);
            }
            inventory.addItem(new ItemStack(Material.ARROW));
        }
    }

    private void configureArmor(Color color, ItemStack[] armor) {
        for(ItemStack piece : armor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) piece.getItemMeta();
            meta.setColor(color);
            piece.setItemMeta(meta);
        }
    }

    private Team configureTeam(String name, List<Player> members, NamedTextColor color) {
        Team team = scoreboard.registerNewTeam(name);
        team.addEntities(members.toArray(Player[]::new));
        team.color(color);
        team.setAllowFriendlyFire(false);
        return team;
    }

    private Score configureScore(String name, NamedTextColor color, List<Player> amount) {
        Score score = playerDistribution.getScore(name);
        score.customName(Component.translatable("minigame.capture.team." + name.toLowerCase(), color));
        score.setScore(amount.size());
        return score;
    }

    private Component formatTeamMessage(CommandSender sender, Component message, TextColor color, String colorKey) {
        Component name = Stardust.getIdentifier(sender).getComponentName().color(color);
        Component colorName = Translation.getTextComponent(sender, "color." + colorKey).color(color);
        Component tagComponent = Component.text("[", color).append(colorName)
                .append(Component.text("]", color));
        return tagComponent.appendSpace().append(name).append(Component.text(": ", color))
                .append(message.color(color));
    }

    void captured(Player capturer, Player captured) {
        Component message;
        boolean checkVictory = false;
        if(capturer == null) {
            message = Component.translatable("minigame.capture.dumb", NamedTextColor.GOLD,
                    captured.name().color(CaptureTeam.getTeam(this, captured).getTextColor()));
            checkVictory = true;
        } else {
            Component capturerName = capturer.name().color(CaptureTeam.getTeam(this, capturer).getTextColor());
            Component capturedName = captured.name().color(CaptureTeam.getTeam(this, captured).getTextColor());
            message = Component.translatable("minigame.capture.captured", NamedTextColor.GOLD, capturedName, capturerName);
            Pair<Integer, Integer> capturerStats = stats.get(capturer);
            capturerStats.setMale(capturerStats.getMale() + 1);
            capturer.playerListName(buildListName(capturer, capturerStats));
        }

        Pair<Integer, Integer> capturedStats = stats.get(captured);
        capturedStats.setFemale(capturedStats.getFemale() + 1);
        captured.playerListName(buildListName(captured, capturedStats));

        CaptureTeam team = CaptureTeam.getTeam(this, captured);
        team.getFreePlayers(this).remove(captured);
        team.getCapturedPlayers(this).add(captured);
        Score score = team.getScore(this);
        score.setScore(score.getScore() - 1);
        captured.teleport(team.other().getBase(this));
        captured.addPotionEffects(CAPTURED_EFFECTS);
        getWorld().getPlayers().forEach(player -> player.sendMessage(message));
        captured.sendMessage(Component.translatable("minigame.capture.you-have-been-captured", NamedTextColor.RED));

        if(checkVictory) {
            checkVictory();
        }
    }

    void saved(Player hero, Player princess) {
        Component message;
        if(hero != null) {
            Component heroName = hero.name().color(CaptureTeam.getTeam(this, hero).getTextColor());
            Component princessName = princess.name().color(CaptureTeam.getTeam(this, princess).getTextColor());
            message = Component.translatable("minigame.capture.saved", NamedTextColor.GOLD, princessName, heroName);
        } else {
            message = null;
        }

        CaptureTeam team = CaptureTeam.getTeam(this, princess);
        team.other().getCapturedPlayers(this).remove(princess);
        team.getFreePlayers(this).add(princess);
        Score score = team.getScore(this);
        score.setScore(score.getScore() + 1);
        princess.teleport(team.getBase(this));
        CAPTURED_EFFECTS.forEach(effect -> princess.removePotionEffect(effect.getType()));

        if(message != null) {
            getWorld().getPlayers().forEach(player -> player.sendMessage(message));
        }
        princess.sendMessage(Component.translatable("minigame.capture.you-have-been-saved", NamedTextColor.GREEN));
    }

    boolean isCaptured(Player player) {
        return blueCaptured.contains(player) || redCaptured.contains(player);
    }

    void deliveryCarry(Player player) {
        CaptureTeam team = CaptureTeam.getTeam(this, player);
        List<Player> carry = this.carry.get(player);
        if(!carry.isEmpty()) {
            for(Player carrying : carry) {
                if(team == CaptureTeam.getTeam(this, carrying)) {
                    saved(player, carrying);
                } else {
                    captured(player, carrying);
                }
            }
            BASE_DELIVERY.play(player);
            carry.clear();
            checkVictory();
        }
    }

    boolean isBeingCarried(Player player) {
        for(List<Player> carryList : carry.values()) {
            if(carryList.contains(player)) {
                return true;
            }
        }
        return false;
    }

    void mount(Player bull, Player bullfighter) {
        if(bull == null) {
            captured(null, bullfighter);
            return;
        }
        List<Player> carry = this.carry.get(bull);
        boolean empty = carry.isEmpty();
        boolean sameTeam = CaptureTeam.areSameTeam(this, bull, bullfighter);
        Component[] messages = new Component[empty ? 2 : 1];
        String bullKey = sameTeam ? "you-are-saving" : "you-are-capturing";
        String bullfighterKey = sameTeam ? "you-are-being-saved" : "you-are-being-captured";
        TextColor bullfighterColor = sameTeam ? NamedTextColor.GREEN : NamedTextColor.RED;
        messages[0] = Component.translatable("minigame.capture." + bullKey, NamedTextColor.GREEN,
                bullfighter.name().color(NamedTextColor.AQUA));
        boolean timer = false;
        Player top;
        if(empty) {
            top = bull;
            messages[1] = Component.translatable("minigame.capture.time", NamedTextColor.GREEN,
                    Component.text(CARRY_TIME, NamedTextColor.GREEN));
            timer = true;
        } else {
            top = carry.getLast();
        }
        top.addPassenger(bullfighter);
        PluginConfig.get().getPlugin().getMessager().message(bull, messages);
        bullfighter.sendMessage(Component.translatable("minigame.capture." + bullfighterKey, bullfighterColor));
        CARRYING.play(bull);
        if(!sameTeam) {
            BEING_CARRIED.play(bullfighter);
        }
        if(timer) {
            CarryTimer carryTimer = new CarryTimer(bull);
            timers.put(bull, carryTimer);
            carryTimer.runTaskTimer(PluginConfig.get().getPlugin(), 0, 20);
        }
    }

    void dismount(Player bull, Player bullfighter) {
        List<Player> carry = this.carry.get(bull);
        int index = carry.indexOf(bullfighter);
        if(index == -1) {
            return;
        }
        bullfighter.leaveVehicle();
        if(index != carry.size() - 1) {
            Player bottom = index == 0 ? bull : carry.get(index - 1);
            Player top = carry.get(index + 1);
            bottom.addPassenger(top);
        }
    }

    void dismountAll(Player bull) {
        boolean teleportToWorldSpawn = WorldUtils.belowVoid(bull.getLocation());
        timers.get(bull).fire();
        List<Player> carry = this.carry.get(bull);
        CaptureTeam bullTeam = CaptureTeam.getTeam(this, bull);
        Component failedToCapture = Component.translatable("minigame.capture.failed-to-capture",
                NamedTextColor.GREEN, bull.name().color(NamedTextColor.GREEN));
        Component failedToSave = Component.translatable("minigame.capture.failed-to-save",
                NamedTextColor.RED, bull.name().color(NamedTextColor.RED));
        carry.forEach(player -> {
            player.leaveVehicle();
            if(bullTeam == CaptureTeam.getTeam(this, player)) {
                player.sendMessage(failedToSave);
            } else {
                player.sendMessage(failedToCapture);
            }
            if(teleportToWorldSpawn) {
                player.teleport(spawn);
            }
        });
        carry.clear();
    }

    Optional<Player> getBull(Player bullfighter) {
        for(Entry<Player, List<Player>> carry : this.carry.entrySet()) {
            if(carry.getValue().contains(bullfighter)) {
                return Optional.of(carry.getKey());
            }
        }
        return Optional.empty();
    }

    void onQuit(Player player) {
        getBull(player).ifPresent(bull -> dismount(bull, player));
        dismountAll(player);
        carry.remove(player);
        CaptureTeam team = CaptureTeam.getTeam(this, player);
        team.getPlayers(this).remove(player);
        if(isCaptured(player)) {
            team.other().getCapturedPlayers(this).remove(player);
        } else {
            team.getFreePlayers(this).remove(player);
        }
        getSnapshot().restore(player);
        Score score = team.getScore(this);
        score.setScore(score.getScore() - 1);
    }

    void onDisconnect(Player player) {
        onQuit(player);
        player.teleport(getInfo().lobby());
    }

    private void checkVictory() {
        for(CaptureTeam team : CaptureTeam.values()) {
            if(team.getFreePlayers(this).isEmpty()) {
                victory(team.other());
                break;
            }
        }
    }

    private void victory(CaptureTeam team) {
        endMatch(team.getPlayers(this), team.other().getPlayers(this));
    }

    private Component buildListName(Player player, Pair<Integer, Integer> stats) {
        Component suffix = Component.text("[" + stats.getMale() + "-" + stats.getFemale() + "]", NamedTextColor.WHITE);
        TextColor color = CaptureTeam.getTeam(this, player).getTextColor();
        return player.name().color(color).appendSpace().append(suffix);
    }

    private Location getLocation(ConfigurationSection section) {
        return new Location(getWorld(), section.getDouble("x"), section.getDouble("y"), section.getDouble("x"));
    }

    @Override
    public List<SquadChannel> getSquadChannels() {
        return List.of(blueChannel, redChannel);
    }

    @Override
    public SquadChannel getSquadChannel(Player player) {
        if(blueChannel.containsParticipant(player)) {
            return blueChannel;
        } else if(redChannel.containsParticipant(player)) {
            return redChannel;
        } else {
            return null;
        }
    }

    private Map<Integer, ItemStack> getShopItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        items.put(0, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_DAMAGE, 1),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 1));
        items.put(1, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_DAMAGE, 2),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 2));
        items.put(2, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_DAMAGE, 3),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 3));
        items.put(9, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_KNOCKBACK, 1),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 1));
        items.put(10, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_KNOCKBACK, 2),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 2));
        items.put(18, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.BOW),
                new Enchant(Enchantment.ARROW_FIRE),
                new Enchant(Enchantment.ARROW_INFINITE)
        ), 2));
        items.put(27, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.KNOCKBACK, 1)
        ), 1));
        items.put(28, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.KNOCKBACK, 2)
        ), 2));
        items.put(36, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.DAMAGE_ALL, 1)
        ), 2));
        items.put(37, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.DAMAGE_ALL, 2)
        ), 3));
        items.put(45, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.FIRE_ASPECT, 1)
        ), 2));
        return items;
    }

    public Location getBlueBase() {
        return blueBase.clone();
    }

    public Location getRedBase() {
        return redBase.clone();
    }

    public Location getSpawn() {
        return spawn.clone();
    }

    private class CarryTimer extends BukkitRunnable {

        private int time = CARRY_TIME;
        private Player player;

        public CarryTimer(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if(time <= 0) {
                fire();
            } else {
                player.setLevel(time--);
            }
        }

        public void fire() {
            cancel();
            dismountAll(player);
            player.setLevel(0);
            timers.remove(player);
        }

    }

}
