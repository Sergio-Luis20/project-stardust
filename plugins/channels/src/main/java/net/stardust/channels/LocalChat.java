package net.stardust.channels;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.stardust.base.events.BaseListener;
import net.stardust.base.model.channel.Channel;
import net.stardust.base.model.channel.Local;
import net.stardust.base.utils.gameplay.MentionService;
import net.stardust.base.utils.SingletonException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@BaseListener
public class LocalChat implements Listener {

    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    private static List<Local> chats;

    private double maxDistance;
    private ChannelsPlugin plugin;
    private MentionService service;

    public LocalChat(ChannelsPlugin plugin) {
        synchronized(loaded) {
            if(!loaded.get()) {
                this.plugin = Objects.requireNonNull(plugin, "plugin");
                maxDistance = plugin.getConfig().getDouble("max-local-distance");
                chats = new ArrayList<>();
                service = MentionService.INSTANCE;
                Bukkit.getWorlds().forEach(this::createLocal);
                loaded.set(true);
            } else {
                throw new SingletonException("LocalChat instance was already created");
            }
        }
    }

    @EventHandler
    public void onLoad(WorldLoadEvent event) {
        createLocal(event.getWorld());
    }

    @EventHandler
    public void onUnload(WorldUnloadEvent event) {
        chats.remove(getLocal(event.getWorld()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerAction(event.getPlayer(), Channel::addParticipant);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerAction(event.getPlayer(), Channel::removeParticipant);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        World from = event.getFrom().getWorld();
        World to = event.getTo().getWorld();
        if(!from.equals(to)) {
            Player player = event.getPlayer();
            getLocal(from).removeParticipant(player);
            getLocal(to).addParticipant(player);
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
        playerAction(event.getPlayer(), (local, player) -> service.mention(player, event.message(), local));
    }

    private void playerAction(Player player, BiConsumer<Local, Player> consumer) {
        World world = player.getWorld();
        Local local = getLocal(world);
        if(local == null) {
            local = createLocal(world);
        }
        consumer.accept(local, player);
    }

    private Local createLocal(World world) {
        Local local = new Local(plugin, world, maxDistance, world.getPlayers());
        chats.add(local);
        return local;
    }

    public static Local getLocal(World world) {
        if(world == null) return null;
        for(Local local : chats) {
            if(local.getWorld().equals(world)) {
                return local;
            }
        }
        return null;
    }

    public static List<Local> getLocals() {
        return Collections.unmodifiableList(chats);
    }

}
