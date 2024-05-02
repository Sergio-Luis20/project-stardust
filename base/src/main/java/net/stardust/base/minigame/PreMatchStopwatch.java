package net.stardust.base.minigame;

import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.utils.SoundPack;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class PreMatchStopwatch extends BukkitRunnable {

    public static final int DEFAULT_PRE_MATCH_TIME = 60;
    public static final SoundPack STARTING = new SoundPack(Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 0.5f);

    @Getter
    private Minigame parent;

    private final int preMatchTime;
    private int time;

    public PreMatchStopwatch(Minigame parent) {
        this(parent, DEFAULT_PRE_MATCH_TIME);
    }

    public PreMatchStopwatch(Minigame parent, int preMatchTime) {
        this.parent = Objects.requireNonNull(parent, "parent");
        if(preMatchTime <= 0) {
            throw new IllegalArgumentException("preMatchTime must be positive");
        }
        this.preMatchTime = preMatchTime;
        time = preMatchTime;
    }

    @Override
    public void run() {
        BossBar bar = parent.getPreMatchBar();
        List<Player> players = parent.getWorld().getPlayers();
        int playerDiff = parent.getInfo().minPlayers() - players.size();
        if(playerDiff > 0) {
            time = preMatchTime;
            bar.name(getBarTitle(playerDiff, true));
            bar.progress(1);
            return;
        }
        if(time <= 0) {
            cancel();
            parent.enterMatchProcess();
        } else {
            bar.name(getBarTitle(time, false));
            bar.progress((float) time / preMatchTime);
            if(time <= 10) {
                players.forEach(STARTING::play);
            }
            time--;
        }
    }

    private Component getBarTitle(int number, boolean player) {
        return Component.translatable("minigame.to-start." + (number == 1 ? "single" : "plural")
                + "." + (player ? "player" : "time"), NamedTextColor.LIGHT_PURPLE,
                Component.text(number, NamedTextColor.AQUA));
    }

}
