package net.stardust.minigames.capture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum CaptureTeam {

    BLUE(NamedTextColor.BLUE, Capture::getBlueTeam, Capture::getBlueBase),
    RED(NamedTextColor.RED, Capture::getRedTeam, Capture::getRedBase);

    private final TextColor textColor;
    private final Function<Capture, List<Player>> playersFunction;
    private final Function<Capture, Location> baseFunction;

    public CaptureTeam other() {
        return this == BLUE ? RED: BLUE;
    }

    public List<Player> getPlayers(Capture capture) {
        return playersFunction.apply(capture);
    }

    public boolean isInThisTeam(Capture capture, Player player) {
        return this == getTeam(capture, player);
    }

    public Location getBase(Capture capture) {
        return baseFunction.apply(capture);
    }

    public static CaptureTeam randomTeam() {
        return ThreadLocalRandom.current().nextBoolean() ? BLUE : RED;
    }

    public static CaptureTeam getTeam(Capture capture, Player player) {
        if(capture.getBlueTeam().contains(player)) {
            return BLUE;
        } else if(capture.getRedTeam().contains(player)) {
            return RED;
        } else {
            throw new IllegalArgumentException("player is not in this Capture match");
        }
    }

    public static boolean areSameTeam(Capture capture, Player p1, Player p2) {
        return getTeam(capture, p1) == getTeam(capture, p2);
    }

    public static boolean areDifferedTeam(Capture capture, Player p1, Player p2) {
        return !areSameTeam(capture, p1, p2);
    }

}
