package net.stardust.minigames.capture;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
@AllArgsConstructor
public enum CaptureTeam {

    BLUE(NamedTextColor.BLUE,
            Capture::getBlueTeam,
            Capture::getBlueFree,
            Capture::getBlueCaptured,
            Capture::getBlueBase,
            Capture::getPolice),
    RED(NamedTextColor.RED,
            Capture::getRedTeam,
            Capture::getRedFree,
            Capture::getRedCaptured,
            Capture::getRedBase,
            Capture::getThief);

    private final TextColor textColor;
    private final Function<Capture, List<Player>> playersFunction, freeFunction, capturedFunction;
    private final Function<Capture, Location> baseFunction;
    private final Function<Capture, Score> scoreFunction;

    public CaptureTeam other() {
        return this == BLUE ? RED: BLUE;
    }

    public List<Player> getPlayers(Capture capture) {
        return playersFunction.apply(capture);
    }

    public List<Player> getFreePlayers(Capture capture) {
        return freeFunction.apply(capture);
    }

    public List<Player> getCapturedPlayers(Capture capture) {
        return capturedFunction.apply(capture);
    }

    public boolean isInsideBase(Capture capture, Player player) {
        return !isOutsideBase(capture, player);
    }

    public boolean isOutsideBase(Capture capture, Player player) {
        final int horizontalLimit = 4;
        final int verticalLimit = 2;

        Location playerLocation = player.getLocation();
        int x = playerLocation.getBlockX();
        int y = playerLocation.getBlockY();
        int z = playerLocation.getBlockZ();

        Location base = getBase(capture);
        int eX = base.getBlockX();
        int eY = base.getBlockY();
        int eZ = base.getBlockZ();

        return x < eX - horizontalLimit ||
                x > eX + horizontalLimit ||
                y > eY + verticalLimit ||
                z < eZ - horizontalLimit ||
                z > eZ + horizontalLimit;
    }

    public boolean isInThisTeam(Capture capture, Player player) {
        return this == getTeam(capture, player);
    }

    public Location getBase(Capture capture) {
        return baseFunction.apply(capture);
    }

    public Score getScore(Capture capture) {
        return scoreFunction.apply(capture);
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
