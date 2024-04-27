package net.stardust.base.command;

import net.stardust.base.BasePlugin;

public abstract non-sealed class AsyncCommand<T extends BasePlugin> extends StardustCommand<T> {
	
	public AsyncCommand(T plugin) {
		super(plugin);
	}
	
	@Override
	final void execute(Runnable task) {
		plugin.getCached().execute(task);
	}
	
}
