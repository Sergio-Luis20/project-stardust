package net.stardust.repository.repositories;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Serializer;
import net.stardust.base.utils.Throwables;
import net.stardust.repository.RepositoryPlugin;

public class FileRepository<K, V extends StardustEntity<K>> extends MapRepository<K, V> {

    private File file;

    public FileRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
        super(plugin, keyClass, valueClass);
    }

    protected static File create(FileRepository<?, ?> fileRepository, File dir, String fileName) {
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		if(!file.exists()) {
			try {
				if(!file.createNewFile()) {
					throw new IOException("Could not create file " + file.getAbsolutePath());
				}
            } catch (IOException e) {
                fileRepository.getPlugin().getLogger().log(Level.SEVERE,
                        "Could not create a file for saving data of a repository", e);
				Throwables.sendAndThrow(fileRepository.getId(), e);
			}
		}
		return file;
	}

    @Override
    protected void doFlush(Map<K, V> elements) {
        synchronized (elements) {
            flushToFile(elements, file);
        }
    }

    protected void flushToFile(Map<K, V> elements, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Serializer.serialize(elements, fos);
        } catch (IOException e) {
            log.log(Level.SEVERE,
                    "Could not serialize the map of elements into the file \"" + file.getAbsolutePath() + "\"", e);
            Throwables.send(getId(), e);
        }
    }

    @Override
    protected Map<K, V> initializeElements() {
        file = create(this, directory(), fileName());
        if (file.length() != 0) {
            return readFile(file);
        }
        return new HashMap<>();
    }

    protected File directory() {
        return new File(getPlugin().getDataFolder(), getId());
    }

    protected String fileName() {
        return getValueClass().getSimpleName() + ".dat";
    }

    @SuppressWarnings("unchecked")
    protected Map<K, V> readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return (Map<K, V>) Serializer.deserialize(fis, Map.class);
        } catch (IOException | ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not read data from file \"" + file.getAbsolutePath() + "\"", e);
            Throwables.sendAndThrow(e);
            return null;
        }
    }
    
}
