package net.stardust.base.utils.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.stardust.base.utils.Throwables;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import br.sergio.utils.FileManager;

public class WorldManager {

    private static long count;

    private Plugin plugin;
    private Path mapsFolder;
    private World currentWorld;
    private Path currentWorldFolder;
    private boolean running;
    private String id;

    public WorldManager(Plugin plugin, Path mapsFolder, String id) {
        this.plugin = Objects.requireNonNull(plugin, "plugin is null");
        this.mapsFolder = Objects.requireNonNull(mapsFolder, "mapsFolder is null");
        this.id = Objects.requireNonNull(id, "id is null");
        validateFolder();
    }

    public void start() {
        if(running) {
            return;
        }
        try {
            Random random = new Random();
            Path map = null;
            List<Path> maps = Files.list(mapsFolder).toList();
            map = maps.get(random.nextInt(maps.size()));
            String folderName = map.getFileName() + "-" + count;
            Path copy = Paths.get(folderName);
            FileManager.copyDirectory(map, copy, true);
            currentWorld = Bukkit.createWorld(new WorldCreator(folderName));
            if(currentWorld != null) {
                currentWorldFolder = copy;
                count++;
                running = true;
            } else {
                FileManager.delete(copy);
            }
        } catch(IOException e) {
            Throwables.sendAndThrow(id, e);
        }
    }

    public void stop() {
        if(!running) {
            return;
        }
        try {
            Bukkit.unloadWorld(currentWorld, false);
            FileManager.delete(currentWorldFolder);
            currentWorld = null;
            currentWorldFolder = null;
            running = false;
        } catch(IOException e) {
            Throwables.sendAndThrow(id, e);
        }
    }

    public String getID() {
        return id;
    }

    public boolean checkWorld(World world) {
        return currentWorld.getName().equals(world.getName());
    }

    public Path getCurrentWorldFolder() {
        return currentWorldFolder;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public Path getMapsFolder() {
        return mapsFolder;
    }

    public boolean isRunning() {
        return running;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    private void validateFolder() {
        if(Files.notExists(mapsFolder)) {
            throw new IllegalArgumentException("mapsFolder does not exist");
        } else if(!Files.isDirectory(mapsFolder)) {
            throw new IllegalArgumentException("mapsFolder is not a directory");
        } else {
            try {
                if(Files.list(mapsFolder).toList().isEmpty()) {
                    throw new IllegalArgumentException("mapsFolder is empty");
                }
            } catch(IOException e) {
                Throwables.sendAndThrow(id, e);
            }
        }
    }

}