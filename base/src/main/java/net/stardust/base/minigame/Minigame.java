package net.stardust.base.minigame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.events.WorldListener;
import net.stardust.base.model.economy.wallet.Currency;
import net.stardust.base.model.economy.wallet.Money;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.minigame.MinigameData;
import net.stardust.base.model.minigame.MinigamePlayer;
import net.stardust.base.utils.PlayerSnapshot;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.database.crud.MinigameDataCrud;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;
import net.stardust.base.utils.plugin.PluginConfig;
import net.stardust.base.utils.world.MapDrawer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
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
    private Listener preMatchTraffic, preMatchListener;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Listener matchListener;

    private PlayerSnapshot snapshot;

    @Getter
    private MinigameState state;
    private List<StateListener> stateListeners;
    private List<TickListener> tickListeners;
    private BukkitRunnable ticker;

    public Minigame(MinigameInfo info) {
        this.info = Objects.requireNonNull(info, "info");
        snapshot = new PlayerSnapshot();
        stateListeners = new ArrayList<>();
        tickListeners = new ArrayList<>();
        postMatchTime = DEFAULT_POST_MATCH_TIME;
        ticker = new BukkitRunnable() {

            @Override
            public void run() {
                tickListeners.forEach(listener -> listener.tick(Minigame.this));
            }

        };
    }

    public void preMatch() {
        setState(MinigameState.PRE_MATCH);
        ticker.runTaskTimer(PluginConfig.get().getPlugin(), 0, 20);
        if(world != null) {
            Bukkit.unloadWorld(world, false);
        }
        world = info.mapDrawer().drawMap();
        if(stopwatch != null) {
            stopwatch.cancel();
        }
        stopwatch = newStopwatch();
        stopwatch.runTaskTimer(PluginConfig.get().getPlugin(), 0, 20);
        if(preMatchTraffic != null) {
            HandlerList.unregisterAll(preMatchTraffic);
        }
        if(preMatchListener != null) {
            HandlerList.unregisterAll(preMatchListener);
        }
        preMatchTraffic = new PreMatchTrafficListener(this);
        preMatchListener = new WorldListener(newPreMatchListener(), world.getName());
        PluginConfig.get().registerEvents(preMatchTraffic, preMatchListener);
        stateListeners.forEach(listener -> listener.onLatePreMatch(this));
    }

    protected Listener newPreMatchListener() {
        return new DefaultPreMatchListener(this);
    }

    protected PreMatchStopwatch newStopwatch() {
        return new PreMatchStopwatch(this);
    }

    protected void endMatch(List<Player> winners, List<Player> losers) {
        setState(MinigameState.END_MATCH);
        unregisterMatchListener();
        MinigameDataCrud dataCrud = new MinigameDataCrud();
        PlayerWalletCrud walletCrud = new PlayerWalletCrud();
        MinigameData data = dataCrud.getOrNull(info.name());
        Map<UUID, PlayerWallet> wallets = walletCrud.getAll(winners.stream()
                .map(Player::getUniqueId)
                .toList())
                .stream()
                .collect(Collectors.toMap(PlayerWallet::getId, wallet -> wallet));
        Map<UUID, MinigamePlayer> minigamePlayers = data.getMinigamePlayers();
        BigInteger reward = new BigInteger(String.valueOf(info.reward()));
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
        PluginConfig.get().getPlugin().getVirtual().submit(() -> {
            dataCrud.update(data);
            walletCrud.updateAll(new ArrayList<>(wallets.values()));
        });
        sendEndMessages(winners, losers);
        Location lobby = info.lobby();
        Bukkit.getScheduler().runTaskLater(PluginConfig.get().getPlugin(), () -> {
            snapshot.restoreAll();
            winners.forEach(p -> p.teleport(lobby));
            losers.forEach(p -> p.teleport(lobby));
            ticker.cancel();
            stateListeners.forEach(listener -> listener.onLateEndMatch(this));
        }, 20 * postMatchTime);
    }

    protected void sendEndMessages(List<Player> winners, List<Player> losers) {
        Money reward = new Money(Currency.SILVER, new BigInteger(String.valueOf(info.reward())));
        Component component = reward.toComponent();
        Component winMessage = Component.translatable("minigame.win", NamedTextColor.GREEN, component);
        Component lossMessage = Component.translatable("minigame.loss", NamedTextColor.RED);
        winners.forEach(p -> p.sendMessage(winMessage));
        losers.forEach(p -> p.sendMessage(lossMessage));
    }

    private static MinigamePlayer getMinigamePlayer(Map<UUID, MinigamePlayer> minigamePlayers, UUID uniqueId) {
        MinigamePlayer minigamePlayer = minigamePlayers.get(uniqueId);
        if(minigamePlayer == null) {
            minigamePlayer = new MinigamePlayer(uniqueId);
            minigamePlayers.put(uniqueId, minigamePlayer);
        }
        return minigamePlayer;
    }

    void enterMatchProcess() {
        setState(MinigameState.MATCH);
        takeSnapshot();
        registerMatchListener();
        match();
    }

    protected abstract void match();

    public void interruptMatch() {
        setState(MinigameState.END_MATCH);
        unregisterMatchListener();
        snapshot.restoreAll();
        Component interruptedMessage = Component.translatable("minigame.match.interrupted", NamedTextColor.RED);
        Location lobby = info.lobby();
        world.getPlayers().forEach(player -> {
            player.sendMessage(interruptedMessage);
            player.teleport(lobby);
        });
        try {
            ticker.cancel();
        } catch(IllegalStateException e) {
            PluginConfig.get().getPlugin().getLogger()
                    .warning("Minigame ticker canceled before being scheduled. Probably because " +
                            "method interruptMatch was called before preMatch state. " + Throwables.send(e));
        }
    }

    protected void setPostMatchTime(int postMatchTime) {
        if(postMatchTime < 0) {
            throw new IllegalArgumentException("postMatchTime must be positive");
        }
        this.postMatchTime = postMatchTime;
    }

    public Location getLobby() {
        return info.lobby().clone();
    }

    void takeSnapshot() {
        snapshot.takeSnapshot(world.getPlayers());
    }

    void registerMatchListener() {
        doWithMatchListener(listener -> PluginConfig.get().registerEvents(listener));
    }

    void unregisterMatchListener() {
        doWithMatchListener(HandlerList::unregisterAll);
    }

    void setState(MinigameState state) {
        this.state = Objects.requireNonNull(state, "state");
        switch(state) {
            case PRE_MATCH -> stateListeners.forEach(listener -> listener.onPreMatch(this));
            case MATCH -> stateListeners.forEach(listener -> listener.onMatch(this));
            case END_MATCH -> stateListeners.forEach(listener -> listener.onEndMatch(this));
        };
    }

    private void doWithMatchListener(Consumer<Listener> action) {
        Listener matchListener = getMatchListener();
        if(matchListener == null) {
            matchListener = this;
        }
        action.accept(matchListener);
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

    public enum MinigameState {
        PRE_MATCH, MATCH, END_MATCH;
    }

}
