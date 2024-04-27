package net.stardust.base.command;

import net.stardust.base.BasePlugin;

public abstract non-sealed class VirtualCommand<T extends BasePlugin> extends StardustCommand<T> {

    public VirtualCommand(T plugin) {
        super(plugin);
    }

    @Override
    final void execute(Runnable task) {
        plugin.getVirtual().execute(task);
    }
    
}
