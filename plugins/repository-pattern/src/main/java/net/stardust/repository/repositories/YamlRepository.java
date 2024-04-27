package net.stardust.repository.repositories;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.stardust.base.Communicable;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.ObjectMapperFactory;
import net.stardust.base.utils.Throwables;
import net.stardust.repository.Repository;
import net.stardust.repository.RepositoryPlugin;

public class YamlRepository<K, V extends StardustEntity<K>> implements Repository<K, V>, Communicable {

    private String id;
	private ObjectMapper mapper;
	private File file;
	private Logger log;
	private Map<K, V> elements;
	private Class<K> keyClass;
	private Class<V> valueClass;
	private ExecutorService flusher;

	public YamlRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		log = plugin.getLogger();
		this.keyClass = Objects.requireNonNull(keyClass, "keyClass");
		this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
		String className = valueClass.getSimpleName();
		id = plugin.getId() + "/" + className;
		mapper = ObjectMapperFactory.yaml();
		file = create(new File(plugin.getDataFolder(), id), className + ".yml", id);
		if(file.length() != 0) {
			try {
				TypeFactory factory = mapper.getTypeFactory();
				elements = mapper.readValue(file, factory.constructMapType(HashMap.class, keyClass, valueClass));
			} catch(IOException e) {
				log.warning("File: " + file.getAbsolutePath() + ", exists: " + file.exists());
				log.warning("Constructor. KeyClass: " + keyClass.getSimpleName() + ", ValueClass: " + valueClass.getSimpleName());
				log.log(Level.WARNING, "Could not create the list from the file, it will be created manually", e);
				elements = new HashMap<>();
			}
		} else {
			elements = new HashMap<>();
		}
		flusher = plugin.getVirtual();
	}
    
    @Override
    public List<V> findAll() {
		return new ArrayList<>(elements.values());
    }

    @Override
	public List<V> findAll(List<K> list) {
		return elements.values().stream().filter(element -> list.contains(element.getEntityId())).collect(Collectors.toList());
	}

    @Override
	public V findById(K element) {
		if(element == null) {
			throw new NullPointerException("findById. KeyValue: " + keyClass.getName() + ". ValueClass: " + valueClass.getName());
		}
		return elements.get(element);
	}

	@Override
	public boolean existsById(K id) {
		if(id == null) {
			throw new NullPointerException("existsById. KeyValue: " + keyClass.getName() + ". ValueClass: " + valueClass.getName());
		}
		return findById(id) != null;
	}
	
	@Override
	public SaveResult save(V data, boolean update) {
		K key = data.getEntityId();
		if(update || !existsById(key)) {
			try {
				elements.put(key, data);
			} catch(Exception e) {
				Throwables.send(getId(), e);
				return SaveResult.FAIL;
			}
		} else {
			return SaveResult.DUPLICATE;
		}
		return flush();
	}

	@Override
	public SaveResult saveAll(List<V> list, boolean update) {
		List<K> keys = list.stream().map(V::getEntityId).toList();
		int size = keys.size();
		for(int i = 0; i < size; i++) {
			K key = keys.get(i);
			if(update || !existsById(key)) {
				try {
					elements.put(key, list.get(i));
				} catch(Exception e) {
					Throwables.send(getId(), e);
					return SaveResult.FAIL;
				}
			} else {
				return SaveResult.DUPLICATE;
			}
		}
		return flush();
	}

	private SaveResult flush() {
		flusher.submit(() -> {
			synchronized(mapper) {
				try {
					mapper.writeValue(file, elements);
				} catch(IOException e) {
					log.log(Level.SEVERE, "Could not save the file " + file
						.getAbsolutePath(), Throwables.send(id, e));
				}
			}
		});
		return SaveResult.SUCCESS;
	}

    @Override
	public boolean delete(K id) {
		try {
			elements.remove(id);
			return flush() == SaveResult.SUCCESS;
		} catch(Exception e) {
			Throwables.send(getId(), e);
			return false;
		}
	}

	@Override
	public boolean deleteAll(List<K> elements) {
		try {
			elements.forEach(this.elements::remove);
			return flush() == SaveResult.SUCCESS;
		} catch(Exception e) {
			Throwables.send(getId(), e);
			return false;
		}
	}

	private static File create(File dir, String fileName, String id) {
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		if(!file.exists()) {
			try {
				if(!file.createNewFile()) {
					throw new IOException("Could not create file " + file.getAbsolutePath());
				}
			} catch(IOException e) {
				Throwables.sendAndThrow(id, e);
			}
		}
		return file;
	}

	@Override
	public String getId() {
		return id;
	}

}
