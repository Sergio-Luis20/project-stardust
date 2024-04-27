package net.stardust.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import br.sergio.comlib.MethodAdapter;
import br.sergio.comlib.ResponseData;
import br.sergio.comlib.ResponseStatus;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.Throwables;

public class RepositoryController<K, V extends StardustEntity<K>> extends MethodAdapter {

	private Repository<K, V> repository;
	private Class<K> keyClass;
	private Class<V> valueClass;

	private RepositoryPlugin plugin;
	
	public RepositoryController(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		repository = RepositoryFactory.getRepository(plugin, keyClass, valueClass);
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.keyClass = Objects.requireNonNull(keyClass, "keyClass");
		this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseData get(Object content) {
		try {
			if(content == null) {
				return new ResponseData(ResponseStatus.OK, (ArrayList<V>) repository.findAll());
			}
			if(keyClass.isInstance(content)) {
				V element = repository.findById(keyClass.cast(content));
				int status = element == null ? ResponseStatus.NOT_FOUND : ResponseStatus.OK;
				return new ResponseData(status, element);
			}
			if(content instanceof List) {
				List<K> ids;
				try {
					ids = (List<K>) content;
				} catch(ClassCastException e) {
					return new ResponseData(ResponseStatus.BAD_REQUEST);
				}
				if(ids.isEmpty()) {
					return new ResponseData(ResponseStatus.OK, new ArrayList<>());
				}
				return new ResponseData(ResponseStatus.OK, (ArrayList<V>) repository.findAll(ids));
			}
			return new ResponseData(ResponseStatus.BAD_REQUEST);
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Exception at method get. KeyClass: " 
					+ keyClass.getName() + ". ValueClass: " + valueClass.getName(), Throwables.send(plugin.getId(), e));
			return new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception: " + e);
		}
	}

	@Override
	public ResponseData post(Object content) {
		return save(content, false);
	}

	@Override
	public ResponseData put(Object content) {
		return save(content, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseData delete(Object content) {
		try {
			if(content == null) {
				return new ResponseData(ResponseStatus.BAD_REQUEST);
			}
			if(keyClass.isInstance(content)) {
				if(repository.delete(keyClass.cast(content))) {
					return new ResponseData(ResponseStatus.NO_CONTENT);
				} else {
					return new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Could not delete element from database");
				}
			}
			if(content instanceof List) {
				List<K> elements;
				try {
					elements = (List<K>) content;
				} catch(ClassCastException e) {
					return new ResponseData(ResponseStatus.BAD_REQUEST);
				}
				if(repository.deleteAll(elements)) {
					return new ResponseData(ResponseStatus.NO_CONTENT);
				} else {
					return new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Could not delete elements from database");
				}
			}
			return new ResponseData(ResponseStatus.BAD_REQUEST);
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Exception at method delete. KeyClass: " 
					+ keyClass.getName() + ". ValueClass: " + valueClass.getName(), Throwables.send(plugin.getId(), e));
			return new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception: " + e);
		}
	}

	@SuppressWarnings("unchecked")
	private ResponseData save(Object content, boolean update) {
		try {
			if(content == null) {
				return new ResponseData(ResponseStatus.BAD_REQUEST);
			}
			if(valueClass.isInstance(content)) {
				return switch(repository.save(valueClass.cast(content), update)) {
					case SUCCESS -> new ResponseData(ResponseStatus.CREATED);
					case DUPLICATE -> new ResponseData(ResponseStatus.CONFLICT);
					case FAIL -> new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Could not " 
							+ (update ? "update" : "create") + " element into database");
				};
			}
			if(content instanceof List) {
				List<V> elements;
				try {
					elements = (List<V>) content;
				} catch(ClassCastException e) {
					return new ResponseData(ResponseStatus.BAD_REQUEST);
				}
				return switch(repository.saveAll(elements, update)) {
					case SUCCESS -> new ResponseData(ResponseStatus.CREATED);
					case DUPLICATE -> new ResponseData(ResponseStatus.CONFLICT);
					case FAIL -> new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Could not " 
							+ (update ? "update" : "create") + " elements into database");
				};
			}
			return new ResponseData(ResponseStatus.BAD_REQUEST);
		} catch(Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Exception at method " + (update ? "put" : "post") + ". KeyClass: " 
					+ keyClass.getName() + ". ValueClass: " + valueClass.getName(), Throwables.send(plugin.getId(), e));
			return new ResponseData(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception: " + e);
		}
	}

}
