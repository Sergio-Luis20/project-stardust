package net.stardust.generalcmd.world;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.generalcmd.GeneralCommandsPlugin;

@BaseCommand("dw")
public class DeleteWorldCommand extends DirectCommand<GeneralCommandsPlugin> {
	
	public DeleteWorldCommand(GeneralCommandsPlugin plugin) {
		super(plugin);
	}
	
	@CommandEntry(opOnly = true)
	public void delete(String name) {
		WorldService.get().deleteWorld(sender(), name);
	}
    
}
