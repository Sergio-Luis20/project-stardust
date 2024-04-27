package net.stardust.base.utils.world;

import br.sergio.utils.FileManager;
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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class FileMapDrawer implements MapDrawer {

    private File folder;

    public FileMapDrawer(File folder) {
        if (!Objects.requireNonNull(folder, "folder").exists()) {
            throw new FileDoesntExistException(folder);
        }
        if (folder.isFile()) {
            throw new IllegalArgumentException("argument \"folder\" must be a directory");
        }
        this.folder = folder;
    }

    @Override
    public World drawMap() {
        File[] subFolders = folder.listFiles(File::isDirectory);
        if (subFolders.length == 0) {
            throw new EmptyDirectoryException(folder);
        }
        int index = ThreadLocalRandom.current().nextInt(0, subFolders.length);
        File map = subFolders[index];
        copyMapToServerFolder(map);
        WorldCreator creator = new WorldCreator(map.getName());
        return creator.createWorld();
    }

    private void copyMapToServerFolder(File map) {
        World world;
        if ((world = Bukkit.getWorld(map.getName())) != null) {
            throw new WorldInUseException(world);
        }
        try {
            File serverFolder = PluginConfig.get().getPlugin().getServerFolder();
            FileUtils.copyDirectory(map, serverFolder);
        } catch(IOException e) {
            Throwables.sendAndThrow(e);
        }
    }

}
