package net.stardust.base.utils.world;

import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.plugin.PluginConfig;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class WorldUtils {

    public static final int MINECRAFT_MIN_LAYER = -64;

    private WorldUtils() {}

    public static World loadWorld(String name) {
        return loadWorld(new WorldCreator(name));
    }

    public static World loadWorld(WorldCreator creator) {
        try {
            return Bukkit.getScheduler().callSyncMethod(PluginConfig.get().getPlugin(), creator::createWorld).get();
        } catch(InterruptedException | ExecutionException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

    public static boolean unloadWorld(World world, boolean save) {
        try {
            return Bukkit.getScheduler().callSyncMethod(PluginConfig.get().getPlugin(), () ->
                    Bukkit.unloadWorld(world, save)).get();
        } catch (InterruptedException | ExecutionException e) {
            Throwables.sendAndThrow(e);
            return false;
        }
    }

    public static boolean unloadWorld(String name, boolean save) {
        try {
            return Bukkit.getScheduler().callSyncMethod(PluginConfig.get().getPlugin(), () ->
                    Bukkit.unloadWorld(name, save)).get();
        } catch (InterruptedException | ExecutionException e) {
            Throwables.sendAndThrow(e);
            return false;
        }
    }

    public static boolean isLoaded(String name) {
        return Bukkit.getWorld(name) != null;
    }

    public static UUID readWorldUUID(File folder) {
        Objects.requireNonNull(folder, "folder");
        if(!folder.exists()) {
            throw new IllegalArgumentException("Folder \"" + folder.getPath() + "\" doesn't exist");
        }
        if(folder.isFile()) {
            throw new IllegalArgumentException("\"" + folder.getPath() + "\" is file");
        }
        File uidDat = new File(folder, "uid.dat");
        try(DataInputStream inputStream = new DataInputStream(new FileInputStream(uidDat))) {
            return new UUID(inputStream.readLong(), inputStream.readLong());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Folder \"" + folder.getPath() + "\" is not a Minecraft " +
                    "map folder. It doesn't have the file uid.dat");
        } catch (IOException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

    public static boolean belowVoid(Location location) {
        Objects.requireNonNull(location);
        Location loc = location.clone();
        for(int i = loc.getBlockY(); i >= MINECRAFT_MIN_LAYER; i--) {
            if(loc.getBlock().getType() != Material.VOID_AIR) {
                return false;
            }
            loc.subtract(0, 1, 0);
        }
        return true;
    }

    public static void deleteUIDDat(String name) throws IOException {
        if(isLoaded(name)) {
            throw buildException("World name: " + name, Bukkit.getWorld(name));
        }
        Files.deleteIfExists(Paths.get(name, "uid.dat"));
    }

    public static File copyMapToServerFolder(File map, String suffix) {
        String finalName = map.getName() + suffix;
        if(isLoaded(finalName)) {
            throw buildException("World name: " + finalName, Bukkit.getWorld(finalName));
        }
        try {
            File serverFolder = PluginConfig.get().getPlugin().getServerFolder();
            File finalFolder = new File(serverFolder, finalName);
            FileUtils.copyDirectory(map, finalFolder);
            deleteUIDDat(finalName);
            return finalFolder;
        } catch(IOException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

    private static WorldInUseException buildException(String message, World world) {
        WorldInUseException e = new WorldInUseException(message);
        e.setWorld(world);
        return e;
    }

}
