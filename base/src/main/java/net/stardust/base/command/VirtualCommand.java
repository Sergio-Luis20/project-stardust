package net.stardust.base.command;

import net.stardust.base.BasePlugin;

/**
 * Represents a command that runs on a virtual thread.
 * That means that senders can access entries concurrently, and
 * because of that, you should manage objects with care, using
 * concurrent and synchronized collections etc... This class is
 * ideal for commands that needs to block a lot, usually in
 * IO operations, but not limited to it. Keep in mind that
 * this runs in other thread than Bukkit Main, so every
 * call to the Bukkit API must be made using Bukkit Scheduler,
 * either by itself, by a {@link BukkitRunnable} object or
 * by {@link StardustThreads} utility class. Also keep in mind
 * that a virtual thread is carried by a platform thread using
 * the ForkJoinPool system, so, as said, it is ideal for blocking,
 * but should not be used to heavy computing operations.
 * 
 * @see AsyncCommand
 * @see SyncCommand
 * @see DirectCommand
 * 
 * @author Sergio Luis
 */
public abstract non-sealed class VirtualCommand<T extends BasePlugin> extends StardustCommand<T> {

    public VirtualCommand(T plugin) {
        super(plugin);
    }

    @Override
    final void execute(Runnable task) {
        plugin.getVirtual().execute(task);
    }
    
}
