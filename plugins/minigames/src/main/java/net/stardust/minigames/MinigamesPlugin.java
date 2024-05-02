package net.stardust.minigames;

import lombok.Getter;
import net.stardust.base.BasePlugin;
import net.stardust.base.minigame.Minigame;
import net.stardust.base.minigame.MinigameInfo;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.persistence.DataManager;
import net.stardust.minigames.signs.MatchSign;
import net.stardust.minigames.signs.MinigameSignData;
import net.stardust.minigames.signs.SignState;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MinigamesPlugin extends BasePlugin {

    public static final NamespacedKey MINIGAME_SIGN = new NamespacedKey("stardust", "minigame_sign");

    private static MinigamesPlugin instance;

    private final Map<String, List<MatchSign>> matches = new HashMap<>();
    private final Map<String, Location> lobbies = new HashMap<>();
    private final File mapsFolder = new File("mgmaps");

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        Location spawn = Bukkit.getWorld("world").getSpawnLocation();
        Bukkit.getOnlinePlayers().forEach(p -> p.teleport(spawn));
        try {
            loadSigns();
        } catch(Exception e) {
            Throwables.sendAndThrow(getId(), e);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Bukkit.getScheduler().cancelTasks(this);
        for(List<MatchSign> list : matches.values()) {
            for(MatchSign sign : list) {
                sign.getMatch().interruptMatch();
            }
        }
    }

    private void loadSigns() throws Exception {
        FileConfiguration config = getConfig();
        ConfigurationSection section = config.getConfigurationSection("minigames");
        for(String key : section.getKeys(false)) {
            List<MatchSign> signs = new ArrayList<>();
            ConfigurationSection minigameSection = section.getConfigurationSection(key);
            String className = minigameSection.getString("class");
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(MinigamesPlugin.class, int.class);
            World lobby = Bukkit.createWorld(new WorldCreator(minigameSection.getString("lobby")));
            List<String> signLocations = minigameSection.getStringList("signs");
            int size = signLocations.size();
            for(int i = 0; i < size; i++) {
                int index = i + 1;
                String location = signLocations.get(i);
                String[] split = location.split(",");
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                int z = Integer.parseInt(split[2]);
                MinigameSignData data = new MinigameSignData(key, index);
                Sign sign = (Sign) lobby.getBlockAt(x, y, z).getState();
                DataManager<Sign> manager = new DataManager<>(sign);
                manager.writeObject(MINIGAME_SIGN, data);
                Location loc = new Location(lobby, x, y, z);
                Minigame match = (Minigame) constructor.newInstance(this, index);
                signs.add(new MatchSign(key, loc, index, match));
            }
            matches.put(key, signs);
            lobbies.put(key, lobby.getSpawnLocation());
        }
        matches.values().forEach(list -> list.getFirst().getMatch().preMatch());
    }

    public void openNewMatch(String key) {
        List<MatchSign> signs = matches.get(key);
        if(signs == null) {
            return;
        }
        for(MatchSign sign : signs) {
            if(sign.getState() == SignState.WAITING) {
                sign.getMatch().preMatch();
                break;
            }
        }
    }

    public Minigame getMatch(Player player) {
        World world = player.getWorld();
        String worldName = world.getName();
        if(!worldName.startsWith("minigame")) {
            return null;
        }
        String[] split = worldName.split("-");
        String minigameName = split[1];
        List<MatchSign> list = null;
        for(String key : matches.keySet()) {
            if(key.equalsIgnoreCase(minigameName)) {
                list = matches.get(key);
                break;
            }
        }
        if(list == null) {
            return null;
        }
        for(MatchSign sign : list) {
            Minigame match = sign.getMatch();
            if(world.equals(match.getWorld())) {
                return match;
            }
        }
        return null;
    }

    public MinigameInfo getMinigameInfo(String minigame, int index) {
        return new MinigameInfo(getConfig().getConfigurationSection("minigames."
                + minigame.toLowerCase()), index);
    }

    public static MinigamesPlugin getPlugin() {
        return instance;
    }

}
