package net.stardust.base.command;

import java.util.concurrent.ExecutorService;

import net.stardust.base.BasePlugin;

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
