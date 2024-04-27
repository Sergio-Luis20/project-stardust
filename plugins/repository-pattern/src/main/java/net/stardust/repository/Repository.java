package net.stardust.repository;

import java.util.List;

import net.stardust.base.model.StardustEntity;

public interface Repository<K, V extends StardustEntity<K>> {

	/**
	 * Returns all values in this repository.
	 * 
	 * @return all values in this repository.
	 */
	List<V> findAll();

	/**
	 * Returns a list of values for a given list
	 * of keys (ids).
	 * 
	 * @param list list of keys (ids).
	 * @return list of values.
	 */
	List<V> findAll(List<K> list);

	/**
	 * Returns a single value for a given key (id).
	 * The behavior of this method if no value
	 * was found depends on its implementation, it
	 * may return null, a default value or throw an
	 * exception.
	 * 
	 * @param id the key.
	 * @return the value, if found.
	 */
	V findById(K id);

	/**
	 * Checks if a value exists into the database for a
	 * given key (id). Implementations should throw a
	 * NullPointerException if the key is null.
	 * 
	 * @param id the key (id) for checking existence.
	 * @return true if the value exists in the repository,
	 * false otherwise.
	 */
	boolean existsById(K id);

	/**
	 * Creates a value into this repository. If there is a
	 * duplicate, then this method will not substitute it. For
	 * updating values, try {@link #save(V, boolean)}.
	 * 
	 * @param data the value to save.
	 * @return the result of the save operation, as described in
	 * the enum SaveResult.
	 */
	default SaveResult save(V data) {
		return save(data, false);
	}

	/**
	 * Creates a value in the repository or updates if it already
	 * exists in case of the parameter {@code update} being
	 * true. If {@code update} is false, then the behavior
	 * of this method should be the same as {@link #save(V)}.
	 * 
	 * @param data the value to save.
	 * @param update true for creating or updating, false for
	 * creating only.
	 * @return the result of the save operation, as described in
	 * the enum SaveResult.
	 */
	SaveResult save(V data, boolean update);

	/**
	 * Saves a list of values into the repository, as described
	 * in {@link #save(V)}. The behavior must be like or must
	 * has the same effect of {@link #save(V)} for each value.
	 * 
	 * @param list the list of values to save.
	 * @return the result of the save operation, as described in
	 * the enum SaveResult.
	 */
	default SaveResult saveAll(List<V> list) {
		return saveAll(list, false);
	}

	/**
	 * Saves a list of values into the repository, as described
	 * in {@link #save(V, boolean)}. The behavior must be like or
	 * must has the same effect of {@link #save(V, boolean)} for
	 * each value.
	 * 
	 * @param list the list of values to save.
	 * @param update true for creating or updating, false for
	 * creating only.
	 * @return the result of the save operation, as described in
	 * the enum SaveResult.
	 */
	SaveResult saveAll(List<V> list, boolean update);

	/**
	 * Deletes a value from this repository. This method returns
	 * true if the value was successfully deleted and false if
	 * the value couldn't be deleted. Trying to delete a value
	 * that doesn't exists should has no effect and return true.
	 * 
	 * @param id the key (id) of the value for delete.
	 * @return boolean true if the value was successfully deleted, false
	 * otherwise.
	 */
	boolean delete(K id);

	/**
	 * Deletes a list of values from this repository as described
	 * in {@link #delete(K)}. The behavior must be like or must to
	 * has the same effect of {@link #delete(K)} for each value.
	 * 
	 * @param list the list of keys (ids) to delete.
	 * @return true if all values were successfully deleted,
	 * false otherwise.
	 */
	boolean deleteAll(List<K> list);

	/**
	 * The final result of a save operation.
	 */
	public static enum SaveResult {

		/**
		 * Should be returned if the save operation
		 * was entirely successfully with the given parameters.
		 */
		SUCCESS,

		/**
		 * Should be returned if the save operation failed
		 * due to the existence of a duplicate.
		 */
		DUPLICATE,

		/**
		 * Should be returnd if the save operation failed by
		 * some other reason.
		 */
		FAIL;

	}

}
