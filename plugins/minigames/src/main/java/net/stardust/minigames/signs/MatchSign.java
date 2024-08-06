package net.stardust.minigames.signs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.database.lang.Translation;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.Minigame.MinigameState;
import net.stardust.base.minigame.MinigameInfo;
import net.stardust.base.minigame.StateListener;
import net.stardust.base.minigame.TickListener;
import net.stardust.base.utils.ranges.Ranges;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class MatchSign {
    
    private Minigame minigame;
    private Location location;
    private Sign sign;
    private SignState state;
    private TickListener tickListener;
    private StateListener stateListener;
    private String key;
    private int index;

    public MatchSign(String key, Location location, int index) {
        this.location = Objects.requireNonNull(location, "location");
        this.index = Ranges.greaterOrEqual(index, 0, "index");
        this.key = Objects.requireNonNull(key, "key");
        state = SignState.WAITING;
        World world = Objects.requireNonNull(location.getWorld(), "location world");
        Block block = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if(block.getState() instanceof Sign signBlock) {
            sign = signBlock;
        } else {
            throw new IllegalArgumentException("block at provided location is not a sign");
        }
        tickListener = minigame -> updateSign();
        stateListener = new StateListener() {

            @Override
            public void onLatePreMatch(Minigame minigame) {
                state = SignState.AVAILABLE;
                updateSign();
            }

            @Override
            public void onMatch(Minigame minigame) {
                state = SignState.RUNNING;
                updateSign();
                MinigamesPlugin.getPlugin().openNewMatch(key);
            }

            @Override
            public void onLateEndMatch(Minigame minigame) {
                state = SignState.WAITING;
                updateSign();
            }

        };
    }
    
    public MatchSign(String key, Location loc, int index, Minigame match) {
        this(key, loc, index);
        setMatch(match);
    }
    
    public Minigame getMatch() {
        return minigame;
    }
    
    public void setMatch(Minigame match) {
        Objects.requireNonNull(match, "match");
        if(minigame != null) {
            minigame.interruptMatch();
            minigame.removeStateListener(stateListener);
            minigame.removeTickListener(tickListener);
        }
        minigame = match;
        minigame.addStateListener(stateListener);
        minigame.addTickListener(tickListener);
        MinigameState matchState = minigame.getState();
        if(matchState != null) {
            state = switch(matchState) {
                case PRE_MATCH -> SignState.AVAILABLE;
                case MATCH -> SignState.RUNNING;
                default -> SignState.WAITING;
            };
        }
        updateSign();
    }

    private void updateSign() {
        updateSign0(minigame.getInfo().lobby().getWorld().getPlayers());
    }

    // aqui originalmente era updateSign0(List<Player> players)
    private void updateSign0(List<Player> players) {
        if(players.isEmpty()) {
            return;
        }
//        MinigameInfo info = minigame.getInfo();
//        Component[] lines = new Component[4];
//        lines[0] = Component.text(info.name() + " " + index);
//        switch(state) {
//            case AVAILABLE -> {
//                lines[1] = Component.translatable("minigame.sign.available", NamedTextColor.GREEN);
//                lines[2] = Component.text(minigame.getWorld().getPlayerCount() + "/"
//                        + info.maxPlayers(), NamedTextColor.WHITE, TextDecoration.BOLD);
//                lines[3] = Component.text("» ONLINE «", NamedTextColor.AQUA, TextDecoration.BOLD);
//            }
//            case RUNNING -> {
//                lines[1] = Component.translatable("minigame.sign.running", NamedTextColor.RED);
//                lines[2] = Component.text(minigame.getWorld().getPlayerCount() + "/"
//                        + info.maxPlayers(), NamedTextColor.WHITE, TextDecoration.BOLD);
//                lines[3] = Component.text("» ONLINE «", NamedTextColor.AQUA, TextDecoration.BOLD);
//            }
//            case WAITING -> {
//                lines[1] = Component.empty();
//                lines[2] = Component.text("-/-", NamedTextColor.WHITE, TextDecoration.BOLD);
//                lines[3] = Component.translatable("minigame.sign.waiting", NamedTextColor.BLUE, TextDecoration.BOLD);
//            }
//        }
//        SignSide side = sign.getSide(Side.FRONT);
//        for(int i = 0; i < lines.length; i++) {
//            side.line(i, lines[i]);
//        }
//        sign.update();

        MinigameInfo info = minigame.getInfo();
        Component title = Component.text(info.name() + " " + index);
        switch(state) {
            case AVAILABLE -> updateActiveSign(info, title, players, player -> Translation
                        .getTextComponent(player, "minigame.sign.available").color(NamedTextColor.GREEN));
            case RUNNING -> updateActiveSign(info, title, players, player -> Translation
                        .getTextComponent(player, "minigame.sign.running").color(NamedTextColor.RED));
            case WAITING -> {
                Component emptyLine = Component.empty();
                Component noPlayers = Component.text("-/-", NamedTextColor.WHITE, TextDecoration.BOLD);
                for(Player player : players) {
                    List<Component> lines = new ArrayList<>(4);
                    lines.add(title);
                    lines.add(emptyLine);
                    lines.add(noPlayers);
                    lines.add(Translation.getTextComponent(player, "minigame.sign.waiting")
                            .color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD));
                    player.sendSignChange(location, lines);
                }
            }
        }
//        Sign virtualSign = (Sign) sign.getBlockData().createBlockState();
//        SignSide side = virtualSign.getSide(Side.FRONT);
//        side.lines().addAll(lines);
//        virtualSign.update();
//        players.forEach(p -> p.sendBlockUpdate(location, virtualSign));
    }

    private void updateActiveSign(MinigameInfo info, Component title, List<Player> players, Function<Player, Component> line2) {
        Component playerCount = Component.text(minigame.getWorld().getPlayerCount() + "/"
                + info.maxPlayers(), NamedTextColor.WHITE, TextDecoration.BOLD);
        Component online = Component.text("» ONLINE «", NamedTextColor.AQUA, TextDecoration.BOLD);
        for(Player player : players) {
            List<Component> lines = new ArrayList<>(4);
            lines.add(title);
            lines.add(line2.apply(player));
            lines.add(playerCount);
            lines.add(online);
            player.sendSignChange(location, lines);
        }
    }

    public boolean isActive() {
        return state == SignState.AVAILABLE || state == SignState.RUNNING;
    }

    public String getLabel() {
        return minigame.getInfo().name() + " " + index;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(o == this) {
            return true;
        }
        if(o instanceof MatchSign matchSign) {
            return location.equals(matchSign.location);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

}
