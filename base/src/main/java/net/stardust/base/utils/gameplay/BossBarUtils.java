package net.stardust.base.utils.gameplay;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class BossBarUtils {

    private BossBarUtils() {}

    public static BossBar newDefaultBar() {
        return BossBar.bossBar(Component.empty(), 1, Color.PINK, Overlay.PROGRESS);
    }

    public static void removeAll(BossBar bar) {
        viewers(bar).forEach(bar::removeViewer);
    }

    public static boolean contains(BossBar bar, Player player) {
        for(BossBarViewer viewer : bar.viewers()) {
            if(viewer.equals(player)) {
                return true;
            }
        }
        return false;
    }

    public static List<Player> viewers(BossBar bar) {
        List<Player> viewers = new ArrayList<>();
        bar.viewers().forEach(viewer -> viewers.add((Player) viewer));
        return viewers;
    }

}
