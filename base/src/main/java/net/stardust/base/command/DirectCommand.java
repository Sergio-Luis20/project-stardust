package net.stardust.base.command;

import org.bukkit.Bukkit;

import net.stardust.base.BasePlugin;

public abstract non-sealed class DirectCommand<T extends BasePlugin> extends StardustCommand<T> {
	
	public DirectCommand(T plugin) {
		super(plugin);
	}
	
	@Override
	final void execute(Runnable task) {
		Bukkit.getScheduler().runTask(plugin, task);
	}
	
}
