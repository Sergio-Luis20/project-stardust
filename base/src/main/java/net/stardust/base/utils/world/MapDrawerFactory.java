package net.stardust.base.utils.world;

import java.io.File;
import java.util.Objects;

public final class MapDrawerFactory {

    private MapDrawerFactory() {}

    public static MapDrawer fromDirectory(String path, String suffix) {
        File file = new File(Objects.requireNonNull(path, "path"));
        if(!file.exists()) throw new IllegalArgumentException("File \"" + path + "\" doesn't exist");
        return new FileMapDrawer(file, suffix);
    }

}
