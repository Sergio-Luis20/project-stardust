package net.stardust.minigames.capture;

import br.sergio.utils.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.Stardust;
import net.stardust.base.database.crud.MinigameDataCrud;
import net.stardust.base.database.lang.Translation;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.MinigameShop;
import net.stardust.base.minigame.SquadChannel;
import net.stardust.base.minigame.SquadMinigame;
import net.stardust.base.model.minigame.MinigameData;
import net.stardust.base.model.minigame.MinigamePlayer;
import net.stardust.base.utils.SoundPack;
import net.stardust.base.utils.inventory.InventoryUtils;
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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
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
            new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2, false, false, true),
            new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false, true)
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

    private Scoreboard scoreboard;
    private Objective playerDistribution;

    @Getter(AccessLevel.PACKAGE)
    private Score police, thief;

    private Map<Player, Pair<Integer, Integer>> stats;
    private Map<Player, List<Player>> carry;
    private Map<Player, CarryTimer> timers;

    @Getter(AccessLevel.PACKAGE)
    private List<Player> blueFree, blueCaptured, redFree, redCaptured;

    private Map<Player, ArmorStand> invisibleEntities;

    @Getter(AccessLevel.PACKAGE)
    private boolean ending;

    public Capture(MinigamesPlugin plugin, int index) {
        super(plugin.getMinigameInfo("capture", index));
        this.plugin = Objects.requireNonNull(plugin, "plugin");

        setTrackerListener(new CaptureTrackerListener(this));
        setMatchListener(new CaptureMatchListener(this));
        setShop(MinigameShop.newShop(this, false, Component.translatable("word.shop"),
                InventoryUtils.DEFAULT_INVENTORY_SIZE, getShopItems()));
    }

    private void initFields() {
        blueTeam = new ArrayList<>();
        redTeam = new ArrayList<>();

        stats = new HashMap<>();
        carry = new HashMap<>();
        timers = new HashMap<>();

        blueFree = new ArrayList<>();
        blueCaptured = new ArrayList<>();
        redFree = new ArrayList<>();
        redCaptured = new ArrayList<>();

        invisibleEntities = new HashMap<>();
    }

    @Override
    protected void match() {
        initFields();
        distributePlayers();
        setupBasesAndSpawn();
        setupSquadChannels();
        setupScoreboard();
        equipPlayers();
        teleportPlayers();
    }

    @Override
    protected void onSpawnCommand(Player player) {
        leave(player);
    }

    @Override
    protected void onMatchInterrupted() {
        invisibleEntities.values().forEach(Entity::remove);
    }

    private void distributePlayers() {
        World world = getWorld();
        List<Player> players = world.getPlayers();
        Collections.shuffle(players);
        CaptureTeam team = CaptureTeam.randomTeam();
        for(Player player : players) {
            Pair<Integer, Integer> statsPair = new Pair<>(0, 0);
            stats.put(player, statsPair);
            carry.put(player, new ArrayList<>());
            player.playerListName(buildListName(player, statsPair, team));
            ArmorStand entity = (ArmorStand) world.spawnEntity(player.getLocation(),
                    EntityType.ARMOR_STAND, SpawnReason.CUSTOM);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            player.addPassenger(entity);
            invisibleEntities.put(player, entity);
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

        blueTeam.forEach(player -> player.setScoreboard(scoreboard));
        redTeam.forEach(player -> player.setScoreboard(scoreboard));

        playerDistribution = scoreboard.registerNewObjective("Players", Criteria.DUMMY,
                Component.translatable("word.players", NamedTextColor.GOLD, TextDecoration.BOLD));
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
        blueTeam.forEach(player -> teleportInWorld(player, blueBase));
        redTeam.forEach(player -> teleportInWorld(player, redBase));
    }

    private void equipTeam(List<Player> team, Color color) {
        for(Player player : team) {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armor = {
                    new ItemStack(Material.LEATHER_BOOTS),
                    new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_CHESTPLATE),
                    new ItemStack(Material.LEATHER_HELMET)
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

    private Component buildListName(Player player, Pair<Integer, Integer> stats, CaptureTeam team) {
        Component suffix = Component.text("[" + stats.getMale() + "-" + stats.getFemale() + "]", NamedTextColor.WHITE);
        return player.name().color(team.getTextColor()).appendSpace().append(suffix);
    }

    void teleportInWorld(Player player, Location location) {
        ArmorStand entity = invisibleEntities.get(player);
        entity.leaveVehicle();
        player.teleport(location);
        player.addPassenger(entity);
    }

    void leave(Player player) {
        player.teleport(getInfo().lobby());
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
            capturer.playerListName(buildListName(capturer, capturerStats, CaptureTeam.getTeam(this, capturer)));
        }

        CaptureTeam capturedTeam = CaptureTeam.getTeam(this, captured);

        Pair<Integer, Integer> capturedStats = stats.get(captured);
        capturedStats.setFemale(capturedStats.getFemale() + 1);
        captured.playerListName(buildListName(captured, capturedStats, capturedTeam));

        capturedTeam.getFreePlayers(this).remove(captured);
        capturedTeam.getCapturedPlayers(this).add(captured);
        Score score = capturedTeam.getScore(this);
        score.setScore(score.getScore() - 1);
        teleportInWorld(captured, capturedTeam.other().getBase(this));
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
        team.getCapturedPlayers(this).remove(princess);
        team.getFreePlayers(this).add(princess);
        Score score = team.getScore(this);
        score.setScore(score.getScore() + 1);

        if(hero != null) {
            princess.leaveVehicle();
            getWorld().getPlayers().forEach(player -> player.sendMessage(message));
        }

        teleportInWorld(princess, hero != null ? team.getBase(this) : spawn);
        princess.clearActivePotionEffects();

        princess.sendMessage(Component.translatable("minigame.capture.you-have-been-saved", NamedTextColor.GREEN));
    }

    boolean isCaptured(Player player) {
        return blueCaptured.contains(player) || redCaptured.contains(player);
    }

    void deliveryCarry(Player player) {
        CaptureTeam team = CaptureTeam.getTeam(this, player);
        List<Player> carry = this.carry.get(player);
        if(!carry.isEmpty()) {
            List<Player> copy = new ArrayList<>(carry);
            carry.clear();
            int size = copy.size();
            for(int i = size - 1; i >= 0; i--) {
                Player carrying = copy.get(i);
                if(team == CaptureTeam.getTeam(this, carrying)) {
                    saved(player, carrying);
                } else {
                    captured(player, carrying);
                }
            }
            fireTimer(player);
            BASE_DELIVERY.play(player);
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
        ArmorStand top;
        if(empty) {
            top = invisibleEntities.get(bull);
            messages[1] = Component.translatable("minigame.capture.time", NamedTextColor.GREEN,
                    Component.text(CARRY_TIME, NamedTextColor.GREEN));
            timer = true;
        } else {
            top = invisibleEntities.get(carry.getLast());
        }
        top.addPassenger(bullfighter);
        carry.add(bullfighter);
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
        carry.remove(index);
        bullfighter.leaveVehicle();
        if(index != carry.size()) {
            Player bottom = index == 0 ? bull : carry.get(index - 1);
            Player top = carry.get(index);
            bottom.addPassenger(top);
        }
    }

    void dismountAll(Player bull) {
        List<Player> carry = this.carry.get(bull);
        if(carry.isEmpty()) {
            return;
        }
        boolean teleportToWorldSpawn = WorldUtils.belowVoid(bull.getLocation());
        fireTimer(bull);
        CaptureTeam bullTeam = CaptureTeam.getTeam(this, bull);
        Component failedToCapture = Component.translatable("minigame.capture.failed-to-capture",
                NamedTextColor.GREEN, bull.name().color(NamedTextColor.GREEN));
        Component failedToSave = Component.translatable("minigame.capture.failed-to-save",
                NamedTextColor.RED, bull.name().color(NamedTextColor.RED));
        List<Player> copy = new ArrayList<>(carry);
        carry.clear();
        copy.forEach(player -> {
            player.leaveVehicle();
            if(bullTeam == CaptureTeam.getTeam(this, player)) {
                player.sendMessage(failedToSave);
            } else {
                player.sendMessage(failedToCapture);
            }
            if(teleportToWorldSpawn) {
                teleportInWorld(player, spawn);
            }
        });
    }

    Optional<Player> getBull(Player bullfighter) {
        for(Entry<Player, List<Player>> carry : this.carry.entrySet()) {
            if(carry.getValue().contains(bullfighter)) {
                return Optional.of(carry.getKey());
            }
        }
        return Optional.empty();
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
        ending = true;
		stopMatchStopwatch();
		timers.values().forEach(timer -> timer.fire(false));
        invisibleEntities.forEach((player, entity) -> {
            entity.leaveVehicle();
            entity.remove();
        });
        String key = team == CaptureTeam.BLUE ? "police-win" : "thief-win";
        Component winMessage = Component.translatable("minigame.capture." + key, NamedTextColor.GOLD, TextDecoration.BOLD);
        getWorld().getPlayers().forEach(player -> player.sendMessage(winMessage));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for(Player player : new ArrayList<>(team.other().getCapturedPlayers(this))) {
                saved(null, player);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                endMatch(team.getPlayers(this), team.other().getPlayers(this));
                ending = false;
            }, 5 * 20);
        }, 5 * 20);
    }

    void onQuit(Player player) {
        getBull(player).ifPresent(bull -> {
            dismount(bull, player);

            CaptureTeam bullTeam = CaptureTeam.getTeam(this, bull);
            CaptureTeam quitterTeam = CaptureTeam.getTeam(this, player);
            if(bullTeam == quitterTeam) {
                return;
            }

            Component capturerName = bull.name().color(bullTeam.getTextColor());
            Component capturedName = player.name().color(quitterTeam.getTextColor());
            Component message = Component.translatable("minigame.capture.captured", NamedTextColor.GOLD, capturedName, capturerName);

            getWorld().getPlayers().forEach(p -> p.sendMessage(message));

            Pair<Integer, Integer> capturerStats = stats.get(bull);
            capturerStats.setMale(capturerStats.getMale() + 1);
            bull.playerListName(buildListName(bull, capturerStats, bullTeam));

            plugin.getVirtual().submit(() -> {
                String name = getInfo().name();
                MinigameDataCrud dataCrud = new MinigameDataCrud();
                MinigameData data = dataCrud.getOrNull(name);
                if(data == null) {
                    data = new MinigameData(name);
                }
                Map<UUID, MinigamePlayer> minigamePlayers = data.getMinigamePlayers();
                UUID playerId = player.getUniqueId();
                MinigamePlayer minigamePlayer = minigamePlayers.get(playerId);
                if(minigamePlayer == null) {
                    minigamePlayer = new MinigamePlayer(playerId);
                    minigamePlayers.put(playerId, minigamePlayer);
                }
                minigamePlayer.setLosses(minigamePlayer.getLosses() + 1);
                dataCrud.update(data);
            });

            player.sendMessage(Component.translatable("minigame.capture.quit-while-being-carried",
                    NamedTextColor.YELLOW, bull.name().color(bullTeam.getTextColor())));
        });
        dismountAll(player);
        carry.remove(player);
        invisibleEntities.remove(player).remove();
        CaptureTeam team = CaptureTeam.getTeam(this, player);
        team.getPlayers(this).remove(player);
        getSquadChannel(player).removeParticipant(player);
        if(isCaptured(player)) {
            team.getCapturedPlayers(this).remove(player);
        } else {
            team.getFreePlayers(this).remove(player);
        }
        getSnapshot().restore(player);
        Score score = team.getScore(this);
        score.setScore(score.getScore() - 1);
        checkVictory();
    }

    private Location getLocation(ConfigurationSection section) {
        return new Location(getWorld(), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
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
                new Enchant(Enchantment.DAMAGE_ALL, 1)
        ), 2));
        items.put(28, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.DAMAGE_ALL, 2)
        ), 3));
        items.put(36, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.KNOCKBACK, 1)
        ), 1));
        items.put(37, MinigameShop.createShopItem(ItemUtils.withEnchants(
                new ItemStack(Material.WOODEN_SWORD),
                new Enchant(Enchantment.KNOCKBACK, 2)
        ), 2));
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

    private void fireTimer(Player bull) {
        Optional.ofNullable(timers.get(bull)).ifPresent(timer -> {
            if(!timer.isCancelled()) {
                timer.fire(false);
            }
        });
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
                fire(true);
            } else {
                player.setLevel(time);
                player.setExp((float) time / CARRY_TIME);
                time--;
            }
        }

        public void fire(boolean dismountAll) {
            cancel();
            if(dismountAll) {
                dismountAll(player);
            }
            player.setLevel(0);
            player.setExp(0);
            timers.remove(player);
        }

    }

}
