package net.stardust.base;

import lombok.Getter;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.message.Messager;
import net.stardust.base.utils.plugin.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Getter
public abstract class BasePlugin extends JavaPlugin implements Communicable {
    
	/*
	 * Todas as subclasses devem chamar super.onLoad(), super.onEnable()
	 * e super.onDisable() caso os sobrescrevam. No caso do super.onDisable(),
	 * ele deve sempre ser chamado dentro de um bloco finally caso não seja a
	 * única operação a ser feita.
	 */
	
	private ExecutorService cached, virtual;
	private List<ExecutorService> singles;
	private PluginConfig pluginConfig;
	private Messager messager;
	private int executorShutdownTimeout = 10;

    @Override
	public void onLoad() {
		singles = Collections.synchronizedList(new ArrayList<>());
		cached = StardustThreads.cachedDaemon();
		virtual = Executors.newVirtualThreadPerTaskExecutor();
        messager = new Messager(this);
		pluginConfig = PluginConfig.get();
		pluginConfig.setPlugin(this);
    }

    @Override
    public void onEnable() {
		saveDefaultConfig();
    	pluginConfig.registerAll();
    }
    
    @Override
    public void onDisable() {
    	shutdownExecutorService(cached);
		shutdownExecutorService(virtual);
    	singles.forEach(this::shutdownExecutorService);
    }
    
    public ExecutorService newSingle() {
    	ExecutorService single = StardustThreads.singleThreadDaemon();
    	singles.add(single);
    	return single;
    }

	protected void setExecutorShutdownTimeout(int executorShutdownTimeout) {
		if(executorShutdownTimeout < 0) {
			throw new IllegalArgumentException("executorShutdownTimeout must be positive");
		}
		this.executorShutdownTimeout = executorShutdownTimeout;
	}

	public File getServerFolder() {
		return new File(".");
	}

	@Override
	public String getId() {
		return getName();
	}
    
	// Can be overwritten by subclasses
    protected boolean shutdownExecutorService(ExecutorService executorService) {
		try {
			executorService.shutdown();
			if(!executorService.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
				if(!executorService.awaitTermination(executorShutdownTimeout, TimeUnit.SECONDS)) {
					throw new TimeoutException("ExecutorService couldn't terminate properly");
				}
			}
			return true;
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Exception while stopping executor service", Throwables.send(getId(), e));
			return false;
		}
	}

}
