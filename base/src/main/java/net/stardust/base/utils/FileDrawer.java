package net.stardust.base.utils;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

public final class FileDrawer {

    private FileDrawer() {}

    public static File drawFile(File folder) {
        File[] subFiles = folder.listFiles(File::isDirectory);
        if (subFiles.length == 0) {
            throw new EmptyDirectoryException(folder);
        }
        int index = ThreadLocalRandom.current().nextInt(0, subFiles.length);
        return subFiles[index];
    }

}
