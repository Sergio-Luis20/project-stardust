package net.stardust.base.command;

import org.bukkit.Bukkit;

import net.stardust.base.BasePlugin;

/**
 * Represents a command that runs on Bukkit Main thread.
 * Because of that, the entries defined in this command cannot be
 * executed concurrently. A sender that queries this command in
 * someway will be blocked until the previous ends its process.
 * However, this class is ideal for commands that make much
 * calls to Bukkit API. Since this command runs on Bukkit Main
 * thread, you must keep in mind that blocks or time-consuming
 * computations will lag the server, so avoid using this kind
 * of command whenever possible. As said, it is only ideal for
 * cases where calls to Bukkit API are very frequently at
 * implementation, so it can avoid the use of calling Bukkit
 * Scheduler. For any other case, consider using {@link SyncCommand},
 * {@link AsyncCommand} or {@link VirtualCommand}.
 * 
 * @see SyncCommand
 * @see AsyncCommand
 * @see VirtualCommand
 * 
 * @author Sergio Luis
 */
public abstract non-sealed class DirectCommand<T extends BasePlugin> extends StardustCommand<T> {
	
	public DirectCommand(T plugin) {
		super(plugin);
	}
	
	@Override
	final void execute(Runnable task) {
		Bukkit.getScheduler().runTask(plugin, task);
	}
	
}
