package net.stardust.base.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PlayerSnapshot {

    private Map<Player, Snapshot> snapshots;

    public PlayerSnapshot() {
        snapshots = new HashMap<>();
    }

    public void takeSnapshot(List<Player> players) {
        players.forEach(this::takeSnapshot);
    }

    public void takeSnapshot(Player player) {
        snapshots.put(player, new Snapshot(player));
    }

    public void restore(Player player) {
        Snapshot backup = snapshots.get(player);
        if(backup != null) {
            backup.apply(player);
            snapshots.remove(player);
        }
    }

    public void restoreAll() {
        snapshots.forEach((player, snapshot) -> snapshot.apply(player));
        snapshots.clear();
    }

    public void clearSnapshots() {
        snapshots.clear();
    }

    public Map<Player, Snapshot> getSnapshots() {
        return Collections.unmodifiableMap(snapshots);
    }

    public void removePlayer(Player player) {
        snapshots.remove(player);
    }

    public record Snapshot(ItemStack[] contents, float exp, Collection<PotionEffect> effects,
                           boolean invisible, boolean invulnerable, Component displayName, Scoreboard scoreboard,
                           double health, GameMode gamemode) {

        public Snapshot(Player player) {
            this(player.getInventory().getContents(), player.getExp(), player.getActivePotionEffects(),
                    player.isInvisible(), player.isInvulnerable(), player.displayName(), player.getScoreboard(),
                    player.getHealth(), player.getGameMode());
        }

        public void apply(Player player) {
            player.getInventory().setContents(contents);
            player.setExp(exp);
            player.clearActivePotionEffects();
            player.addPotionEffects(effects);
            player.setInvisible(invisible);
            player.setInvulnerable(invulnerable);
            player.displayName(displayName);
            player.setScoreboard(scoreboard);
            player.setHealth(health);
            player.setGameMode(gamemode);
        }

    }

}
