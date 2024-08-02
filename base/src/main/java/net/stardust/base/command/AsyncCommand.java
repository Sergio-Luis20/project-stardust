package net.stardust.base.command;

import net.stardust.base.BasePlugin;

/**
 * Represents a command that runs in a platform thread other
 * than the Bukkit Main thread. This command uses a cached thread
 * pool, so multiple senders can execute the entries of this command
 * concurrently without need to wait the end of some other sender
 * execution. As this command runs in a cached thread pool, if
 * it will be a command that will be heavy called by senders, it
 * can be expensive in resources to maintain. However, it is ideal for
 * heavy computation operations. Keep in mind that since this command
 * runs in other thread than Bukkit Main, then every Bukkit API call
 * must be made using Bukkit Scheduler, either by itself, by a
 * {@link BukkitRunnable} object of by {@link StardustThreads} utility
 * class. Also keep in mind that this type of command is not made
 * for blocking a lot as if consumes resources, so if it's the case,
 * consider using {@link VirtualCommand}. Also, as the entries of this
 * command can be accessed concurrently, you must manage objects with
 * care, using concurrent and synchronized collections etc...
 * 
 * @see SyncCommand
 * @see DirectCommand
 * @see VirtualCommand
 * 
 * @author Sergio Luis
 */
public abstract non-sealed class AsyncCommand<T extends BasePlugin> extends StardustCommand<T> {
	
	public AsyncCommand(T plugin) {
		super(plugin);
	}
	
	@Override
	final void execute(Runnable task) {
		plugin.getCached().execute(task);
	}
	
}
