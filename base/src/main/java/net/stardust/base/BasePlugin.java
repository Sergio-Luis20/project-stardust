package net.stardust.base;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import jakarta.persistence.EntityManagerFactory;
import net.stardust.base.command.BaseCommand;
import net.stardust.base.database.BaseEntity;
import net.stardust.base.events.BaseListener;
import net.stardust.base.model.StardustEntity;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.message.Messager;
import net.stardust.base.utils.plugin.PluginConfig;

/**
 * This is the superclass of every Stardust plugin. It builds default tools
 * that can be used as a centralized way for the entire application.
 * 
 * @apiNote All subclasses must call <b>super.onLoad()</b>,
 *          <b>super.onEnable()</b>
 *          and <b>super.onDisable()</b> if the respective method is overridden.
 *          In the case
 *          of <b>super.onDisable()</b>, it must be always called inside a
 *          <b>finally</b>
 *          block if there is a minimal chance of throwing an exception before
 *          its execution.
 * 
 * @author Sergio Luis
 */
public abstract class BasePlugin extends JavaPlugin implements Communicable {

	/**
	 * The package where find all implementations of {@link StardustEntity}.
	 * Some classes may be in subpackages.
	 */
	public static final String ENTITIES_PACKAGE = "net.stardust.base.model";

	/**
	 * The default {@link ExecutorService} timeout in seconds used by the
	 * default implementation of {@link #shutdownExecutorService(ExecutorService)}.
	 */
	public static final int DEFAULT_EXECUTOR_SERVICE_TIMEOUT = 10;

	private ExecutorService cached, virtual;
	private List<ExecutorService> singles;
	private PluginConfig pluginConfig;
	private Messager messager;
	private int executorShutdownTimeout;
	private EntityManagerFactory entityManagerFactory;

	/**
	 * The {@link BasePlugin} implementation initializes every attribute that can
	 * be used by the entire plugin application like the 3 main
	 * {@link ExecutorService}s
	 * (the single daemon platform thread one, the cached daemon platform threads
	 * one and
	 * the virtual threads one), the {@link Messager} object for sending messages in
	 * the
	 * Bukkit Main thread (being thread-safe in that context) and the
	 * {@link PluginConfig}
	 * that can be used to get statically this plugin in the entire application. The
	 * <b>super.onLoad()</b> is a must call by subclasses!
	 * 
	 * @see BasePlugin
	 * @see ExecutorService
	 * @see StardustThreads
	 * @see Messager
	 * @see PluginConfig
	 */
	@Override
	public void onLoad() {
		singles = Collections.synchronizedList(new LinkedList<>());
		cached = StardustThreads.cachedDaemon();
		virtual = Executors.newVirtualThreadPerTaskExecutor();
		messager = new Messager(this);
		executorShutdownTimeout = DEFAULT_EXECUTOR_SERVICE_TIMEOUT;
		pluginConfig = PluginConfig.get();
		pluginConfig.setPlugin(this);
		entityManagerFactory = createEntityManagerFactory();
	}

	/**
	 * The {@link BasePlugin} implementation calls
	 * {@link JavaPlugin#saveDefaultConfig()}
	 * and {@link PluginConfig#registerAll()}. Use <b>super.onEnable()</b> to do
	 * automatic
	 * config saving and commands and listeners registration.
	 * 
	 * @see BasePlugin
	 * @see PluginConfig#registerAll()
	 * @see JavaPlugin#saveDefaultConfig()
	 * @see BaseCommand
	 * @see BaseListener
	 */
	@Override
	public void onEnable() {
		saveDefaultConfig();
		pluginConfig.registerAll();
	}

	/**
	 * The {@link BasePlugin} implementation just closes its
	 * {@link ExecutorService}s.
	 * This method do not closes the {@link EntityManagerFactory} if the subclass
	 * overrides
	 * {@link #createEntityManagerFactory()} to provide one. It is you
	 * responsability to close
	 * that created object in the subclass implementation. As said in
	 * {@link BasePlugin}
	 * documentation, if in the subclass implementation the <b>super.onDisable()</b>
	 * statement
	 * has any minimal chance of not be called (for example because there are other
	 * statements
	 * before it that can throw exceptions), you should move that statement to a
	 * <b>finally</b>
	 * block.
	 * 
	 * @see BasePlugin
	 * @see ExecutorService
	 * @see EntityManagerFactory
	 * @see #shutdownExecutorService(ExecutorService)
	 * @see #createEntityManagerFactory()
	 * @see JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		shutdownExecutorService(cached);
		shutdownExecutorService(virtual);
		singles.forEach(this::shutdownExecutorService);
	}

	/**
	 * Creates a new single platform thread executor service, adds it to an internal
	 * collection and then returns the service. The created executor service
	 * creates daemon threads to prevent the application to be alive, and for
	 * that reason, all single platform threads executor services used by the
	 * application should be created using this method for peventing problems.
	 * When this plugin terminates, all those services in the collection are
	 * automatically closed during {@link #onDisable()} for safety. That is one
	 * more reason that you should use this method to create them.
	 * 
	 * @return a new single daemon platform thread executor service
	 */
	public ExecutorService newSingle() {
		ExecutorService single = StardustThreads.singleThreadDaemon();
		singles.add(single);
		return single;
	}

	/**
	 * Must be overriden by subclasses that uses {@link EntityManagerFactorie}s.
	 * Also,
	 * the created object closing process is entire responsability of the subclass,
	 * this is not closed during {@link #onDisable()}.
	 * 
	 * @return a new {@link EntityManagerFactory} instance
	 */
	protected EntityManagerFactory createEntityManagerFactory() {
		return null;
	}

	/**
	 * Sets the default shutdown timeout for {@link ExecutorService}s instances
	 * in this class in seconds. This timeout will be used in {@link #onDisable()}
	 * to close those services.
	 * 
	 * @param executorShutdownTimeout the new executor service shutdown timeout in
	 *                                seconds
	 * @throws IllegalArgumentException if timeout is negative
	 */
	protected void setExecutorShutdownTimeout(int executorShutdownTimeout) {
		if (executorShutdownTimeout < 0) {
			throw new IllegalArgumentException("executorShutdownTimeout must be positive");
		}
		this.executorShutdownTimeout = executorShutdownTimeout;
	}

	/**
	 * Returns the server folder.
	 * 
	 * @implNote This just returns
	 * 
	 *           <pre>
	 * <code>
	 * new File(".");
	 * </code>
	 *           </pre>
	 * 
	 *           because it is the default execution directory for Bukkit.
	 *           If for some reason the execution directory changes,
	 *           this may not return the actual server folder.
	 * 
	 * @return the server folder
	 */
	public File getServerFolder() {
		return new File(".");
	}

	/**
	 * Returns the communication id as specified in {@link Communicable}.
	 * The default implementation for {@link BasePlugin}s is the plugin name.
	 * 
	 * @return the plugin name as its communication id
	 */
	@Override
	public String getId() {
		return getName();
	}

	/**
	 * Returns the cached daemon platform threads executor service
	 * of this plugin.
	 * 
	 * @see ExecutorService
	 * @see Executors#newCachedThreadPool()
	 * @see StardustThreads#cachedDaemon()
	 * @return the cached executor service
	 */
	public ExecutorService getCached() {
		return cached;
	}

	/**
	 * Returns the virtual thread per task executor service of this plugin.
	 * 
	 * @see ExecutorService
	 * @see Executors#newVirtualThreadPerTaskExecutor()
	 * @return the virtual threads executor service
	 */
	public ExecutorService getVirtual() {
		return virtual;
	}

	/**
	 * Returns the messager of this plugin. See {@link Messager} documentation
	 * to know how it works and for what it can be used.
	 * 
	 * @see Messager
	 * @return the messager of this plugin
	 */
	public Messager getMessager() {
		return messager;
	}

	/**
	 * Returns the {@link EntityManagerFactory} provided by the
	 * {@link #createEntityManagerFactory()}
	 * method. The default implementation of that method returns null beucase no
	 * every plugin
	 * will use an entity manager factory. If you need one, override it. Also, as
	 * said in its
	 * documentation, this object, if created, is not closed in {@link #onDisable()}
	 * default
	 * implementation, so it is your responsability to close it.
	 * 
	 * @see EntityManagerFactory
	 * @see #createEntityManagerFactory()
	 * @see #onDisable()
	 * @return the entity manager factory of this plugin or {@code null} if none was
	 *         created
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/**
	 * Returns the plugin config of the entire application. It can
	 * also be obtained by {@link PluginConfig#get()}.
	 * 
	 * @see PluginConfig
	 * @see PluginConfig#get()
	 * @return the application plugin config
	 */
	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	/**
	 * Method used in {@link #onDisable()} to shutdown the {@link ExecutorService}s
	 * of
	 * this plugin. Though it is made for that purpose, it can be used to close any
	 * other executor service than the ones of this plugin if you want to use the
	 * default
	 * closing implementation. This method uses the internal shutdown timeout in
	 * seconds,
	 * which has a default value but you can set your own using
	 * {@link #setExecutorShutdownTimeout(int)}.
	 * If the closing process results in any {@link Exception} (including obviouly a
	 * {@link TimeoutException}), it is logged using the {@link Logger} object of
	 * this
	 * plugin and sent to the throwables manager system. Also, this method can be
	 * overriden
	 * by subclasses if they want to implement a custom close process for the
	 * executor services
	 * in {@link #onDisable()}.
	 * 
	 * @see Logger
	 * @see ExecutorService
	 * @see TimeoutException
	 * @see #onDisable()
	 * @see #setExecutorShutdownTimeout(int)
	 * @param executorService the {@link ExecutorService} to shutdown
	 * @return true if the service was successful closed, false otherwise
	 * @throws NullPointerException if executor service is null
	 */
	protected boolean shutdownExecutorService(ExecutorService executorService) {
		try {
			executorService.shutdown();
			if (!executorService.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				if (!executorService.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
					throw new TimeoutException("ExecutorService couldn't terminate properly");
				}
			}
			return true;
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Exception while stopping executor service", e);
			Throwables.send(getId(), e);
			return false;
		}
	}

	/**
	 * Returns a reflection object for scanning the {@link #ENTITIES_PACKAGE}
	 * with the Scanner of types annotated. Use this to scan entities annotated with
	 * {@link BaseEntity}.
	 * 
	 * @see BaseEntity
	 * @see StardustEntity
	 * @see #ENTITIES_PACKAGE
	 * @see Reflections
	 * @return the reflections object to scan the stardust entities package
	 */
	public static Reflections getStardustEntitiesReflections() {
		return new Reflections(new ConfigurationBuilder()
				.forPackages(ENTITIES_PACKAGE)
				.addScanners(Scanners.TypesAnnotated));
	}

}
