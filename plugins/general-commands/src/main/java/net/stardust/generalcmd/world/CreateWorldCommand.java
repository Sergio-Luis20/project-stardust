package net.stardust.generalcmd.world;

import java.util.ArrayList;
import java.util.List;

import net.stardust.base.command.BaseCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.command.DirectCommand;
import net.stardust.generalcmd.GeneralCommandsPlugin;

@BaseCommand("cw")
public class CreateWorldCommand extends DirectCommand<GeneralCommandsPlugin> {
	
	private WorldService worldService;

    public CreateWorldCommand(GeneralCommandsPlugin plugin) {
		super(plugin);
		worldService = WorldService.get();
	}
    
    @CommandEntry(opOnly = true)
    public void execute(String worldConfig) {
    	String[] args = worldConfig.split(" ");
    	switch(args.length) {
	    	case 1 -> {
	    		worldService.createWorld(sender(), args[0], new ArrayList<>());
	    	}
	    	default -> {
	    		List<String> options = new ArrayList<>();
	    		for(int i = 1; i < args.length; i++) {
	    			options.add(args[i].toLowerCase());
	    		}
	    		worldService.createWorld(sender(), args[0], options);
	    	}
    	};
    }
    
}
