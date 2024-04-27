package net.stardust.generalcmd.world;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.base.command.SenderType;
import net.stardust.generalcmd.GeneralCommandsPlugin;

@BaseCommand(value = "tpw", types = SenderType.PLAYER)
public class TpwCommand extends DirectCommand<GeneralCommandsPlugin> {

    public TpwCommand(GeneralCommandsPlugin plugin) {
        super(plugin);
    }
    
    @CommandEntry(opOnly = true)
    public boolean tpw(String worldName) {
        return WorldService.get().teleport(sender(), worldName);
    }

}
