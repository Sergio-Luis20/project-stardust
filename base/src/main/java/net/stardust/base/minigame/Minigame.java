package net.stardust.base.minigame;

import br.sergio.utils.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.events.DefaultListener;
import net.stardust.base.events.TrackerListener;
import net.stardust.base.events.WorldListener;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.minigame.MinigameData;
import net.stardust.base.model.minigame.MinigamePlayer;
import net.stardust.base.utils.*;
import net.stardust.base.utils.database.crud.MinigameDataCrud;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.plugin.PluginConfig;
import net.stardust.base.utils.world.NotWorldListener;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public abstract class Minigame implements Listener {

    public static final int DEFAULT_POST_MATCH_TIME = 5;

    @Getter
    private MinigameInfo info;

    private int postMatchTime;

    @Getter
    private World world;

    @Getter
    private PreMatchStopwatch stopwatch;

    @Getter(AccessLevel.PACKAGE)
    private BossBar preMatchBar;
    private BossBar matchBar;
    private BukkitRunnable matchStopwatch;
    private Listener preMatchTraffic;
    private Listener defaultListener;

    private Listener matchListener;

    @Setter(AccessLevel.PROTECTED)
    private TrackerListener trackerListener;

    @Getter(AccessLevel.PROTECTED)
    private PlayerSnapshot snapshot;

    @Getter
    private MinigameState state;
    private List<StateListener> stateListeners;
    private List<TickListener> tickListeners;
    private BukkitRunnable ticker;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private MinigameShop shop;

    private ExecutorService mapCopyExecutorService;

    public Minigame(MinigameInfo info) {
        this.info = Objects.requireNonNull(info, "info");
        snapshot = new PlayerSnapshot();
        stateListeners = new ArrayList<>();
        tickListeners = new ArrayList<>();
        postMatchTime = DEFAULT_POST_MATCH_TIME;
        preMatchBar = BossBarUtils.newDefaultBar();
        matchBar = BossBarUtils.newDefaultBar();
        mapCopyExecutorService = PluginConfig.get().getPlugin().getCached();
        ticker = new BukkitRunnable() {

            @Override
            public void run() {
                tickListeners.forEach(listener -> listener.tick(Minigame.this));
            }

        };
        ticker.runTaskTimer(PluginConfig.get().getPlugin(), 0, 20);
    }

    public void preMatch() {
        setState(MinigameState.PRE_MATCH);
        if(stopwatch != null) {
            stopwatch.cancel();
        }
        if(world != null) {
            Bukkit.unloadWorld(world, false);
            world = null;
        }
        mapCopyExecutorService.submit(() -> {
            File drawnMap = FileDrawer.drawFile(info.mapsFolder());
            String suffix = "-" + info.index();
            WorldUtils.copyMapToServerFolder(drawnMap, suffix);
            Bukkit.getScheduler().runTask(PluginConfig.get().getPlugin(),
                    () -> preMatch0(drawnMap.getName() + suffix));
        });
    }

    private void preMatch0(String worldName) {
        world = WorldUtils.loadWorld(worldName);
        world.getEntities().forEach(Entity::remove);
        stopwatch = newStopwatch();
        stopwatch.runTaskTimer(PluginConfig.get().getPlugin(), 0, 20);
        preMatchTraffic = new PreMatchTrafficListener(this);
        defaultListener = newPreMatchListener();
        PluginConfig.get().registerEvents(preMatchTraffic, defaultListener);
        registerListener(shop);
        registerListener(trackerListener);
        stateListeners.forEach(listener -> listener.onLatePreMatch(this));
    }

    protected Listener newPreMatchListener() {
        return new DefaultListener(this::getWorld);
    }

    protected PreMatchStopwatch newStopwatch() {
        return new PreMatchStopwatch(this);
    }

    protected void endMatch(List<Player> winners, List<Player> losers) {
        setState(MinigameState.END_MATCH);
        unregisterMatchListener();
        unregisterShop();
        unregisterListener(trackerListener);
        registerListener(defaultListener);
        stopMatchStopwatch();
        MatchResult result = getMatchResult(winners, losers);
        if(result != MatchResult.NO_WINNERS) {
            MinigameDataCrud dataCrud = new MinigameDataCrud();
            PlayerWalletCrud walletCrud = new PlayerWalletCrud();
            MinigameData data = dataCrud.getOrNull(info.name());
            if(data == null) {
                data = new MinigameData(info.name());
            }
            Map<UUID, PlayerWallet> wallets = walletCrud.getAll(winners.stream()
                            .map(Player::getUniqueId)
                            .toList())
                    .stream()
                    .collect(Collectors.toMap(PlayerWallet::getId, wallet -> wallet));
            Map<UUID, MinigamePlayer> minigamePlayers = data.getMinigamePlayers();
            BigInteger reward = new BigInteger(String.valueOf(info.reward()));
            if(result == MatchResult.NORMAL) {
                onNormalEnding(winners, losers, wallets, minigamePlayers, reward);
            } else {
                onDrawEnding(wallets, minigamePlayers, reward);
            }
            final MinigameData finalData = data;
            PluginConfig.get().getPlugin().getVirtual().submit(() -> {
                dataCrud.update(finalData);
                walletCrud.updateAll(new ArrayList<>(wallets.values()));
            });
        }
        sendEndMessages(winners, losers, result);
        Location lobby = info.lobby();
        Bukkit.getScheduler().runTaskLater(PluginConfig.get().getPlugin(), () -> {
            snapshot.restoreAll();
            BossBarUtils.removeAll(preMatchBar);
            BossBarUtils.removeAll(matchBar);
            getWorld().getPlayers().forEach(player -> player.teleport(lobby));
            unregisterPreMatchListeners();
            unregisterMatchListener();
            unregisterShop();
            stateListeners.forEach(listener -> listener.onLateEndMatch(this));
            Bukkit.unloadWorld(world, false);
            world = null;
        }, 20 * postMatchTime);
    }

    private void onNormalEnding(List<Player> winners, List<Player> losers, Map<UUID, PlayerWallet> wallets,
                                Map<UUID, MinigamePlayer> minigamePlayers, BigInteger reward) {
        for(Player player : winners) {
            UUID uniqueId = player.getUniqueId();
            MinigamePlayer minigamePlayer = getMinigamePlayer(minigamePlayers, uniqueId);
            minigamePlayer.setWins(minigamePlayer.getWins() + 1);
            PlayerWallet wallet = wallets.get(uniqueId);
            wallet.getSilver().add(reward);
        }
        for(Player player : losers) {
            UUID uniqueId = player.getUniqueId();
            MinigamePlayer minigamePlayer = getMinigamePlayer(minigamePlayers, uniqueId);
            minigamePlayer.setLosses(minigamePlayer.getLosses() + 1);
        }
    }

    private void onDrawEnding(Map<UUID, PlayerWallet> wallets, Map<UUID, MinigamePlayer> minigamePlayers, BigInteger reward) {
        for(Player player : getWorld().getPlayers()) {
            UUID uniqueId = player.getUniqueId();
            MinigamePlayer minigamePlayer = getMinigamePlayer(minigamePlayers, uniqueId);
            minigamePlayer.setWins(minigamePlayer.getWins() + 1);
            PlayerWallet wallet = wallets.get(uniqueId);
            wallet.getSilver().add(reward);
        }
    }

    protected void sendEndMessages(List<Player> winners, List<Player> losers, MatchResult result) {
        switch(result) {
            case NORMAL -> {
                Money reward = new Money(Currency.SILVER, new BigInteger(String.valueOf(info.reward())));
                Component component = reward.toComponent();
                Component winMessage = Component.translatable("minigame.win", NamedTextColor.GREEN, component);
                Component lossMessage = Component.translatable("minigame.loss", NamedTextColor.RED);
                sendEndMessages0(new Pair<>(winners, winMessage), new Pair<>(losers, lossMessage));
            }
            case DRAW -> {
                Component drawMessage = Component.translatable("minigame.draw", NamedTextColor.YELLOW,
                        TextDecoration.BOLD);
                sendEndMessages0(new Pair<>(winners, drawMessage), new Pair<>(losers, drawMessage));
            }
            case NO_WINNERS -> {
                Component noWinnersMessage = Component.translatable("minigame.no-winners",
                        NamedTextColor.GOLD, TextDecoration.BOLD);
                sendEndMessages0(new Pair<>(winners, noWinnersMessage), new Pair<>(losers, noWinnersMessage));
            }
        }
    }

    protected final void sendEndMessages0(Pair<List<Player>, Component> winnersMessage,
                                          Pair<List<Player>, Component> losersMessage) {
        winnersMessage.getMale().forEach(player -> player.sendMessage(winnersMessage.getFemale()));
        losersMessage.getMale().forEach(player -> player.sendMessage(losersMessage.getFemale()));
    }

    private static MinigamePlayer getMinigamePlayer(Map<UUID, MinigamePlayer> minigamePlayers, UUID uniqueId) {
        MinigamePlayer minigamePlayer = minigamePlayers.get(uniqueId);
        if(minigamePlayer == null) {
            minigamePlayer = new MinigamePlayer(uniqueId);
            minigamePlayers.put(uniqueId, minigamePlayer);
        }
        return minigamePlayer;
    }

    private static MatchResult getMatchResult(List<Player> winners, List<Player> losers) {
        if(winners.isEmpty()) {
            return losers.isEmpty() ? MatchResult.DRAW : MatchResult.NO_WINNERS;
        }
        return MatchResult.NORMAL;
    }

    void enterMatchProcess() {
        unregisterPreMatchListeners();
        BossBarUtils.removeAll(preMatchBar);
        BossBarUtils.removeAll(matchBar);
        match();
        registerMatchListener();
        startMatchBar();
        setState(MinigameState.MATCH);
    }

    protected abstract void match();

    protected abstract void onSpawnCommand(Player player);

    protected abstract void onMatchInterrupted();

    private void startMatchBar() {
        matchStopwatch = new BukkitRunnable() {

            private int totalTime = info.matchTime();
            private int time = totalTime;

            @Override
            public void run() {
                if(time <= 0) {
                    endMatch(Collections.emptyList(), Collections.emptyList());
                } else {
                    String minutes = toFormattedString(time / 60);
                    String seconds = toFormattedString(time % 60);
                    String timeString = minutes + ":" + seconds;
                    TextColor color;
                    Color barColor;
                    if(time <= totalTime / 4) {
                        color = NamedTextColor.RED;
                        barColor = Color.RED;
                    } else if(time <= totalTime / 2) {
                        color = NamedTextColor.YELLOW;
                        barColor = Color.YELLOW;
                    } else {
                        color = NamedTextColor.GREEN;
                        barColor = Color.GREEN;
                    }
                    matchBar.name(Component.text(timeString, color));
                    matchBar.color(barColor);
                    matchBar.progress((float) time / totalTime);
                    time--;
                }
            }

            private String toFormattedString(int number) {
                return (number < 10 ? "0" : "") + number;
            }

        };
    }

    public final void spawnCommandIssued(Player player) {
        switch(state) {
            case PRE_MATCH -> {
                removePlayerFromBars(player);
                player.teleport(info.lobby());
                snapshot.restore(player);
            }
            case MATCH -> {
                removePlayerFromBars(player);
                onSpawnCommand(player);
            }
            case END_MATCH -> player.sendMessage(Component.translatable("minigame.spawn-in-end-match", NamedTextColor.YELLOW));
            default -> {
                IllegalStateException e = new IllegalStateException("Player " + player.getName()
                        + " (UID: " + player.getUniqueId() + ") issued spawn command in minigame "
                        + info.name() + " without a predefined state: " + Arrays.toString(MinigameState.values()));
                player.sendMessage(AutomaticMessages.internalServerError());
                Throwables.sendAndThrow(e);
            }
        }
    }

    public final void interruptMatch() {
        if(state == MinigameState.MATCH) {
            onMatchInterrupted();
        }
        setState(MinigameState.END_MATCH);
        unregisterListener(trackerListener);
        unregisterPreMatchListeners();
        unregisterMatchListener();
        stopMatchStopwatch();
        if(world != null) {
            Component interruptedMessage = Component.translatable("minigame.match.interrupted",
                    NamedTextColor.RED);
            Location lobby = info.lobby();
            world.getPlayers().forEach(player -> {
                removePlayerFromBars(player);
                player.sendMessage(interruptedMessage);
                player.teleport(lobby);
            });
            Bukkit.unloadWorld(world, false);
            world = null;
        }
        snapshot.restoreAll();
    }

    private void stopMatchStopwatch() {
        if(matchStopwatch != null) {
            try {
                matchStopwatch.cancel();
            } catch(IllegalStateException e) {
                // ignored
            } finally {
                BossBarUtils.removeAll(matchBar);
                matchStopwatch = null;
            }
        }
    }

    protected void setPostMatchTime(int postMatchTime) {
        if(postMatchTime < 0) {
            throw new IllegalArgumentException("postMatchTime must be positive");
        }
        this.postMatchTime = postMatchTime;
    }

    private void removePlayerFromBars(Player player) {
        preMatchBar.removeViewer(player);
        matchBar.removeViewer(player);
    }

    private void registerMatchListener() {
        if(matchListener == null) {
            matchListener = this;
        } else if(!(matchListener instanceof WorldListener)) {
            NotWorldListener e = new NotWorldListener("Class: " + matchListener.getClass().getName());
            e.setListener(matchListener);
            throw e;
        }
        PluginConfig.get().registerEvents(matchListener);
    }

    private void unregisterMatchListener() {
        unregisterListener(matchListener);
    }

    private void unregisterPreMatchListeners() {
        unregisterListener(preMatchTraffic);
        unregisterDefaultListener();
    }

    private void unregisterDefaultListener() {
        unregisterListener(defaultListener);
    }

    private void unregisterShop() {
        unregisterListener(shop);
    }

    private void unregisterListener(Listener listener) {
        if(listener != null) {
            HandlerList.unregisterAll(listener);
        }
    }

    private void registerListener(Listener listener) {
        if(listener != null) {
            PluginConfig.get().registerEvents(listener);
        }
    }

    private void setState(MinigameState state) {
        this.state = Objects.requireNonNull(state, "state");
        switch(state) {
            case PRE_MATCH -> stateListeners.forEach(listener -> listener.onPreMatch(this));
            case MATCH -> stateListeners.forEach(listener -> listener.onMatch(this));
            case END_MATCH -> stateListeners.forEach(listener -> listener.onEndMatch(this));
        };
    }

    public void addStateListener(StateListener stateListener) {
        stateListeners.add(Objects.requireNonNull(stateListener, "stateListener"));
    }

    public void removeStateListener(StateListener stateListener) {
        stateListeners.remove(Objects.requireNonNull(stateListener, "stateListener"));
    }

    public void addTickListener(TickListener tickListener) {
        tickListeners.add(Objects.requireNonNull(tickListener, "tickListener"));
    }

    public void removeTickListener(TickListener tickListener) {
        tickListeners.remove(Objects.requireNonNull(tickListener, "tickListeners"));
    }

    public boolean containsStateListener(StateListener stateListener) {
        return stateListeners.contains(stateListener);
    }

    public boolean containsTickListener(TickListener tickListener) {
        return tickListeners.contains(tickListener);
    }

    public final Listener getMatchListener() {
        return matchListener;
    }

    protected final void setMatchListener(Listener matchListener) {
        this.matchListener = matchListener;
    }

    public boolean useTracker() {
        return trackerListener != null;
    }

    public enum MinigameState {
        PRE_MATCH, MATCH, END_MATCH;
    }

    public enum MatchResult {
        NORMAL, DRAW, NO_WINNERS;
    }

}
