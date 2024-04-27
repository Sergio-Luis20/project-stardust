package net.stardust.base.utils.world;

import net.stardust.base.utils.ProviderNotFoundException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Objects;

public final class MapDrawerFactory {

    private MapDrawerFactory() {}

    public static MapDrawer fromDirectory(String path) {
        File file = new File(Objects.requireNonNull(path, "path"));
        if(!file.exists()) throw new IllegalArgumentException("File \"" + path + "\" doesn't exist");
        return new FileMapDrawer(file);
    }

}
