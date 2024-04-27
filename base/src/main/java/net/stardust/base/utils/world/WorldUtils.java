package net.stardust.base.utils.world;

import net.stardust.base.utils.Throwables;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public final class WorldUtils {

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

}
