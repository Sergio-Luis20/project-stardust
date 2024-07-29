package net.stardust.repository.repositories;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.ObjectMapperFactory;
import net.stardust.base.utils.Throwables;
import net.stardust.repository.RepositoryPlugin;

public class YamlRepository<K, V extends StardustEntity<K>> extends FileRepository<K, V> {

	private ObjectMapper mapper;

	public YamlRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		super(plugin, keyClass, valueClass);
		mapper = ObjectMapperFactory.yaml();
	}

	@Override
	protected void flushToFile(Map<K, V> elements, File file) {
		synchronized (elements) {
			try {
				mapper.writeValue(file, elements);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Could not save elements into file \"" + file.getAbsolutePath() + "\"", e);
				Throwables.send(getId(), e);
			}
		}
	}

	@Override
	protected String fileName() {
		return getValueClass().getSimpleName() + ".yml";
	}
	
	@Override
	protected Map<K, V> readFile(File file) {
		try {
			TypeFactory factory = mapper.getTypeFactory();
			return mapper.readValue(file, factory.constructMapType(HashMap.class, getKeyClass(), getValueClass()));
		} catch (IOException e) {
			log.log(Level.WARNING, "Could not create the map from the file, therefore a new empty map instance will be created", e);
			log.warning("File: \"" + file.getAbsolutePath() + "\". Exists: " + file.exists());
			log.warning("Key class: " + getKeyClass().getSimpleName() + ". Value class: "
					+ getValueClass().getSimpleName());
			return new HashMap<>();
		}
	}

}
