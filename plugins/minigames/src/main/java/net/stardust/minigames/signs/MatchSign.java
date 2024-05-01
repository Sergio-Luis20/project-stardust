package net.stardust.minigames.signs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.MinigameInfo;
import net.stardust.base.minigame.StateListener;
import net.stardust.base.minigame.TickListener;
import net.stardust.base.utils.ranges.Ranges;
import net.stardust.minigames.MinigamesPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            public void onPreMatch(Minigame minigame) {
                MinigamesPlugin.getPlugin().openNewMatch(key);
            }

            @Override
            public void onLatePreMatch(Minigame minigame) {
                state = SignState.AVAILABLE;
                updateSign();
            }

            @Override
            public void onMatch(Minigame minigame) {
                state = SignState.RUNNING;
                updateSign();
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
        if(minigame != null) {
            minigame.interruptMatch();
        }
        minigame = Objects.requireNonNull(match, "match");
        minigame.addStateListener(stateListener);
        minigame.addTickListener(tickListener);
        updateSign();
    }

    private void updateSign() {
        updateSign0(minigame.getInfo().lobby().getWorld().getPlayers());
    }

    private void updateSign0(List<Player> players) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        List<Component> lines = new ArrayList<>();
        MinigameInfo info = minigame.getInfo();
        lines.add(serializer.deserialize(info.name() + " " + index));
        switch(state) {
            case AVAILABLE -> {
                lines.add(Component.translatable("minigame.sign.available", NamedTextColor.GREEN));
                lines.add(Component.text(minigame.getWorld().getPlayerCount() + "/" + info.maxPlayers(),
                        NamedTextColor.WHITE, TextDecoration.BOLD));
                lines.add(Component.text("» ONLINE «", NamedTextColor.AQUA, TextDecoration.BOLD));
            }
            case RUNNING -> {
                lines.add(Component.translatable("minigame.sign.running", NamedTextColor.RED));
                lines.add(Component.text(minigame.getWorld().getPlayerCount() + "/" + info.maxPlayers(),
                        NamedTextColor.WHITE, TextDecoration.BOLD));
                lines.add(Component.text("» ONLINE «", NamedTextColor.AQUA, TextDecoration.BOLD));
            }
            case WAITING -> {
                lines.add(Component.empty());
                lines.add(Component.text("-/-", NamedTextColor.WHITE, TextDecoration.BOLD));
                lines.add(Component.translatable("minigame.sign.waiting", NamedTextColor.RED, TextDecoration.BOLD));
            }
        }
        Sign virtualSign = (Sign) sign.getBlockData().createBlockState();
        SignSide side = virtualSign.getSide(Side.FRONT);
        side.lines().addAll(lines);
        virtualSign.update();
        players.forEach(p -> p.sendBlockUpdate(location, virtualSign));
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
