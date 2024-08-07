package net.stardust.base.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.bukkit.Bukkit;

import net.stardust.base.BasePlugin;
import net.stardust.base.utils.plugin.PluginConfig;

public final class StardustThreads {
    
	public static final ThreadFactory DAEMON_FACTORY = runnable -> {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	};
	
    private StardustThreads() {}
    
    public static ExecutorService singleThreadDaemon() {
    	return Executors.newSingleThreadExecutor(DAEMON_FACTORY);
    }

    public static ExecutorService cachedDaemon() {
        return Executors.newCachedThreadPool(DAEMON_FACTORY);
    }

	public static void run(Runnable runnable) {
		run(PluginConfig.get().getPlugin(), runnable);
	}
    
    public static void run(BasePlugin plugin, Runnable runnable) {
    	Bukkit.getScheduler().runTask(plugin, runnable);
    }
    
    public static void runAndWait(Runnable runnable) {
		runAndWait(PluginConfig.get().getPlugin(), runnable);
	}

	public static void runAndWait(BasePlugin plugin, Runnable runnable) {
    	CompletableFuture<Void> future = new CompletableFuture<>();
    	Bukkit.getScheduler().runTask(plugin, () -> {
    		try {
    			runnable.run();
    		} finally {
    			future.complete(null);
    		}
    	});
    	future.join();
    }

	public static <T> T call(Callable<T> callable) {
		return call(PluginConfig.get().getPlugin(), callable);
	}
    
    public static <T> T call(BasePlugin plugin, Callable<T> callable) {
    	if(Bukkit.isPrimaryThread()) {
    		try {
				return callable.call();
			} catch(Exception e) {
				Throwables.sendAndThrow(plugin.getId(), e);
			}
    	} else {
    		try {
				return Bukkit.getScheduler().callSyncMethod(plugin, callable).get();
			} catch(InterruptedException | ExecutionException e) {
				Throwables.sendAndThrow(plugin.getId(), e);
			}
    	}
    	return null;
    }

}
