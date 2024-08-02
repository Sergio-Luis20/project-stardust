package net.stardust.base.command;

import java.util.concurrent.ExecutorService;

import net.stardust.base.BasePlugin;

/**
 * Represents a command that runs in a single thread other
 * than the Bukkit Main thread. This is ideal for managing
 * internal objects without the need of synchronization and
 * other concurrent features if those objects are not being
 * managed by other threads too. Keep in mind that as this
 * command runs in other thread than Bukkit Main, every call
 * to Bukkit API must be made using Bukkit Scheduler, either
 * by itself, by a {@link BukkitRunnable} object or by {@link
 * StardustThreads} utility class. Also remember that every
 * call to the entries of this command is executed by only one
 * thread, and because of that, if that thread blocks a lot or
 * do heavy computation and this command is heavy queried by
 * senders, it will have a bad performance. For those cases
 * consider using {@link VirtualCommand} for blocking commands
 * and {@link AsyncCommand} for heavy computations.
 * 
 * @see AsyncCommand
 * @see VirtualCommand
 * @see DirectCommand
 * 
 * @author Sergio Luis
 */
public abstract non-sealed class SyncCommand<T extends BasePlugin> extends StardustCommand<T> {
	
	private ExecutorService executorService;

	public SyncCommand(T plugin) {
		super(plugin);
		executorService = plugin.newSingle();
	}
	
	@Override
	final void execute(Runnable task) {
		executorService.execute(task);
	}

}
