package net.stardust.repository;

import net.stardust.base.model.StardustEntity;
import net.stardust.repository.repositories.YamlRepository;

public final class RepositoryFactory {

	public static <K, V extends StardustEntity<K>> Repository<K, V> getRepository(RepositoryPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		return new YamlRepository<>(plugin, keyClass, valueClass);
	}

}
