package net.stardust.base.utils.world;

import net.stardust.base.utils.Throwables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

public final class WorldUtils {

    public static final int MINECRAFT_MIN_LAYER = -64;

    private WorldUtils() {}

    public static World getLoadedWorld(String name) {
        WorldCreator creator = new WorldCreator(name);
        return creator.createWorld();
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
        if(!uidDat.exists()) {
            throw new IllegalArgumentException("Folder \"" + folder.getPath() + "\" is not a Minecraft " +
                    "map folder. It doesn't have the file uid.dat");
        }
        try(DataInputStream inputStream = new DataInputStream(new FileInputStream(uidDat))) {
            return new UUID(inputStream.readLong(), inputStream.readLong());
        } catch (FileNotFoundException e) {
            throw new Error(e);
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

}
