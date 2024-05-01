package net.stardust.base.minigame;

import net.stardust.base.utils.ranges.Ranges;
import net.stardust.base.utils.world.MapDrawer;
import net.stardust.base.utils.world.MapDrawerFactory;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public record MinigameInfo(String name, int minPlayers, int maxPlayers, int reward, int matchTime,
                           MapDrawer mapDrawer, Location lobby, int index) {

    public MinigameInfo {
        if(Objects.requireNonNull(name, "name").isBlank())
            throw new IllegalArgumentException("blank name");
        name = name.trim();
        minPlayers = Ranges.greaterOrEqual(minPlayers, 2, "minPlayers");
        maxPlayers = Ranges.greaterOrEqual(maxPlayers, 2, "minPlayers");
        if(minPlayers > maxPlayers)
            throw new IllegalArgumentException("minPlayers must not be greater than maxPlayers");
        reward = Ranges.greater(reward, 0, "reward");
        matchTime = Ranges.greater(matchTime, 0, "matchTime");
        Objects.requireNonNull(mapDrawer, "mapDrawer");
        Objects.requireNonNull(lobby, "lobby");
    }

    public MinigameInfo(ConfigurationSection section, int index) {
        this(section.getString("name"), section.getInt("minPlayers"), section.getInt("maxPlayers"),
                section.getInt("reward"), section.getInt("matchTime"), getMapDrawer(section, index),
                WorldUtils.getLoadedWorld(section.getString("lobby")).getSpawnLocation(), index);
    }

    @Override
    public Location lobby() {
        return lobby.clone();
    }

    private static MapDrawer getMapDrawer(ConfigurationSection section, int index) {
        return MapDrawerFactory.fromDirectory(section.getString("map-path"), "-" + index);
    }

}
