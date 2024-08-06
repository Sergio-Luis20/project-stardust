package net.stardust.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.BasePlugin;
import net.stardust.base.database.crud.UserCrud;
import net.stardust.base.database.lang.Translation;
import net.stardust.base.events.BaseListener;
import net.stardust.base.model.user.User;
import net.stardust.base.utils.StardustThreads;

@BaseListener
public class StardustAuthentication extends BasePlugin implements Listener {

    private int minPwLength, maxTime, interval;

    private UserCrud userCrud;

    private Map<UUID, AuthorizationMessage> messages;
    private List<UUID> waiting;

    @Override
    public void onEnable() {
    	super.onEnable();

        messages = new ConcurrentHashMap<>();
        waiting = Collections.synchronizedList(new ArrayList<>());

        FileConfiguration config = getConfig();
        minPwLength = config.getInt("min-pw-length");
        maxTime = config.getInt("max-time");
        interval = config.getInt("authorization-messages-interval");

        userCrud = new UserCrud();

        Translation.load();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws IOException {
        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();
        waiting.add(uid);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if(waiting.contains(uid)) {
                unauthorize(player, uid);
            }
        }, 20 * maxTime);
        getVirtual().submit(() -> {
            User user = userCrud.getOrNull(uid);
            String key = user == null ? "register.message" : "login.message";
            Component message = Component.translatable(key, NamedTextColor.YELLOW);
            AuthorizationMessage authMessage = new AuthorizationMessage(this, player, message);
            messages.put(uid, authMessage);
            authMessage.start();
        });
        return;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        AuthorizationMessage message = messages.remove(id);
        if(message != null) {
            message.cancel();
        }
        waiting.remove(id);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        int spaceIndex = command.indexOf(" ");
        command = command.substring(1, spaceIndex == -1 ? command.length() : spaceIndex).toLowerCase();
        Command login = getCommand("login");
        Command register = getCommand("register");
        event.setCancelled(waiting.contains(event.getPlayer().getUniqueId()) && checkNotCommand(login, command) && checkNotCommand(register, command));
    }

    private boolean checkNotCommand(Command command, String str) {
        return !command.getName().equals(str) && !command.getAliases().contains(str);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(waiting.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player && waiting.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public  int getMinPwLength() {
        return minPwLength;
    }
    
    public boolean isWaiting(UUID uid) {
    	return waiting.contains(uid);
    }
    
    public void authorize(UUID uid) {
    	waiting.remove(uid);
		AuthorizationMessage message = messages.remove(uid);
		if(message != null) {
			message.cancel();
		}
    }

    public void unauthorize(Player player, UUID uid) {
        unauthorize(player, uid, "kick.unauthorized");
    }

    public void unauthorize(Player player, UUID uid, String translationKey) {
        waiting.remove(uid);
        AuthorizationMessage message = messages.remove(uid);
        if(message != null) {
            message.cancel();
        }
        StardustThreads.run(this, () -> {
        	if(player.isOnline()) {
                player.kick(Component.translatable(translationKey, NamedTextColor.RED, 
                    Component.text(maxTime, NamedTextColor.RED)));
        	}
        });
    }

    private class AuthorizationMessage extends BukkitRunnable {

        private Plugin plugin;
        private Player player;
        private Component message;

        public AuthorizationMessage(Plugin plugin, Player player, Component message) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
            this.player = Objects.requireNonNull(player, "player");
            this.message = Objects.requireNonNull(message, "message");
        }

        public void start() {
            runTaskTimer(plugin, 0, 20 * interval);
        }

        @Override
        public void run() {
            player.sendMessage(message);
        }

    }

}
