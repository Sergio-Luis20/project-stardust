package net.stardust.base.events;

import br.sergio.utils.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.utils.gameplay.Tracker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.function.Supplier;

public class TrackerListener extends WorldListener {

    private double maxTrackDistance;

    public TrackerListener(Supplier<World> supplier) {
        this(supplier, Double.POSITIVE_INFINITY);
    }

    public TrackerListener(Supplier<World> supplier, double maxTrackDistance) {
        super(supplier);
        if(maxTrackDistance < 0) {
            throw new IllegalArgumentException("maxTrackDistance must be positive");
        }
        this.maxTrackDistance = maxTrackDistance;
    }

    @EventHandler
    public void onTracker(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if(checkWorld(world)) {
            Action action = event.getAction();
            ItemStack tracker = event.getItem();
            if((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                    && Tracker.isTracker(tracker)) {
                Location loc = player.getLocation();
                List<Player> players = getPlayersToTrack(player, world).stream().filter(p ->
                        p.getLocation().distance(loc) <= maxTrackDistance).toList();
                Pair<Player, Double> tracked = new Pair<>(null, Double.POSITIVE_INFINITY);
                for(Player p : players) {
                    double distance = p.getLocation().distance(loc);
                    if(distance < tracked.getFemale()) {
                        tracked.setMale(p);
                        tracked.setFemale(distance);
                    }
                }
                player.sendMessage(getMessage(tracked));
                Player trackedPlayer = tracked.getMale();
                CompassMeta meta = (CompassMeta) tracker.getItemMeta();
                meta.setLodestoneTracked(false);
                meta.setLodestone(trackedPlayer == null ? world.getSpawnLocation() : trackedPlayer.getLocation());
                tracker.setItemMeta(meta);
            }
        }
    }

    protected List<Player> getPlayersToTrack(Player user, World world) {
        List<Player> players = world.getPlayers();
        players.remove(user);
        return players;
    }

    private static Component getMessage(Pair<Player, Double> tracked) {
        Player trackedPlayer = tracked.getMale();
        Component trackedName;
        if(trackedPlayer == null) {
            trackedName = Component.text("null", NamedTextColor.GRAY);
        } else {
            trackedName = Component.text(trackedPlayer.getName(), NamedTextColor.GRAY);
        }
        Component trackedDistance;
        double distance = tracked.getFemale();
        if(Double.isInfinite(distance)) {
            trackedDistance = Component.translatable("word.infinity", NamedTextColor.GRAY);
        } else {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(trackedPlayer.locale());
            DecimalFormat format = new DecimalFormat("#.##", symbols);
            trackedDistance = Component.text(format.format(distance), NamedTextColor.GRAY);
        }
        return Component.translatable("track", NamedTextColor.GRAY, trackedName, trackedDistance);
    }

}
