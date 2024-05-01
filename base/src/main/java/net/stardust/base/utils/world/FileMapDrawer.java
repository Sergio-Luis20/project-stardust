package net.stardust.base.utils.world;

import lombok.Getter;
import net.stardust.base.utils.EmptyDirectoryException;
import net.stardust.base.utils.FileDoesntExistException;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.plugin.PluginConfig;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class FileMapDrawer implements MapDrawer {

    private File folder;
    private String suffix;

    public FileMapDrawer(File folder, String suffix) {
        if (!Objects.requireNonNull(folder, "folder").exists()) {
            throw new FileDoesntExistException(folder);
        }
        if (folder.isFile()) {
            throw new IllegalArgumentException("argument \"folder\" must be a directory");
        }
        this.folder = folder;
        this.suffix = suffix == null ? "" : suffix;
    }

    @Override
    public World drawMap() {
        File[] subFolders = folder.listFiles(File::isDirectory);
        if (subFolders.length == 0) {
            throw new EmptyDirectoryException(folder);
        }
        int index = ThreadLocalRandom.current().nextInt(0, subFolders.length);
        File map = subFolders[index];
        File finalMap = copyMapToServerFolder(map);
        WorldCreator creator = new WorldCreator(finalMap.getName());
        return creator.createWorld();
    }

    private File copyMapToServerFolder(File map) {
        String mapName = map.getName();
        String finalName = mapName + suffix;
        World world;
        if ((world = Bukkit.getWorld(finalName)) != null) {
            throw new WorldInUseException(world);
        }
        try {
            File serverFolder = PluginConfig.get().getPlugin().getServerFolder();
            FileUtils.copyDirectory(map, serverFolder);
            Path current = new File(serverFolder, mapName).toPath();
            return Files.move(current, current.resolveSibling(finalName)).toFile();
        } catch(IOException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

}
