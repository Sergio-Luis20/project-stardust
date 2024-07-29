package net.stardust.repository;

import java.lang.reflect.Constructor;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.stardust.base.model.StardustEntity;

public final class RepositoryFactory {

	@SuppressWarnings("unchecked")
	public static <K, V extends StardustEntity<K>> Repository<K, V> getRepository(RepositoryPlugin plugin,
			Class<K> keyClass, Class<V> valueClass) throws RepositoryException {
		try {
			FileConfiguration config = plugin.getConfig();
			ConfigurationSection repositorySection = config.getConfigurationSection("repository");
			if (repositorySection == null) {
				throw new RepositoryException("Missing ConfigurationSection \"repository\" in config.yml");
			}
			List<String> implementations = repositorySection.getStringList("implementations");
			if (implementations == null || implementations.isEmpty()) {
				throw new RepositoryException("Missing or empty list of " + Repository.class.getName() + " implementations class names");
			}
			int current = repositorySection.getInt("current", -1);
			if (current < 0 || current >= implementations.size()) {
				throw new RepositoryException("Current selected repository implementation not valid: missing or index out of bounds");
			}
			String className = implementations.get(current);
			Class<?> repositoryClass = Class.forName(className);
			if (!Repository.class.isAssignableFrom(repositoryClass)) {
				throw new RepositoryException("Current selected class doesn't implement " + Repository.class.getName());
			}
			Constructor<?> constructor = repositoryClass.getConstructor(RepositoryPlugin.class, Class.class,
					Class.class);
			return (Repository<K, V>) constructor.newInstance(plugin, keyClass, valueClass);
		} catch (ClassNotFoundException e) {
			throw new RepositoryException("Repository class not found", e);
		} catch (NoSuchMethodException e) {
			throw new RepositoryException(
					"Repository class without default public constructor with parameters [%s, %s, %s]"
							.formatted(RepositoryPlugin.class.getName(), Class.class.getName(), Class.class.getName()),
					e);
		} catch (Exception e) {
			throw new RepositoryException("Could not instantiate repository class instance", e);
		}
	}

}
