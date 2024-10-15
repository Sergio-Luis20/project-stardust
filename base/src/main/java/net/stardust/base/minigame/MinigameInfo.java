package net.stardust.base.minigame;

import net.stardust.base.utils.ranges.Ranges;
import net.stardust.base.utils.world.WorldUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Objects;

public record MinigameInfo(String name, int minPlayers, int maxPlayers, int reward, int matchTime,
                           File mapsFolder, Location lobby, int index) {

    public MinigameInfo {
        name = name.trim();
        if(Objects.requireNonNull(name, "name").isEmpty())
            throw new IllegalArgumentException("empty or blank name");
        Ranges.greaterOrEqual(minPlayers, 2, "minPlayers");
        Ranges.greaterOrEqual(maxPlayers, 2, "minPlayers");
        if(minPlayers > maxPlayers)
            throw new IllegalArgumentException("minPlayers must not be greater than maxPlayers");
        Ranges.greater(reward, 0, "reward");
        Ranges.greater(matchTime, 0, "matchTime");
        validateMapsFolder(mapsFolder);
        Objects.requireNonNull(lobby, "lobby");
    }

    public MinigameInfo(ConfigurationSection section, int index) {
        this(
                section.getString("name"),
                section.getInt("min-players"),
                section.getInt("max-players"),
                section.getInt("reward"),
                section.getInt("match-time"),
                new File(section.getString("map-path")),
                WorldUtils.loadWorld(section.getString("lobby")).getSpawnLocation(),
                index
        );
    }

    @Override
    public Location lobby() {
        return lobby.clone();
    }

    private static void validateMapsFolder(File mapsFolder) {
        Objects.requireNonNull(mapsFolder, "mapsFolder");
        if(!mapsFolder.exists())
            throw new IllegalArgumentException("maps folder doesn't exist");
        if(mapsFolder.isFile())
            throw new IllegalArgumentException("maps folder must be a folder, but is file");
        if(mapsFolder.listFiles(File::isDirectory).length == 0)
            throw new IllegalArgumentException("maps folder must contain at least 1 map");
    }

}
