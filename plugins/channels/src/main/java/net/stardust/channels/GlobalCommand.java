package net.stardust.channels;

import net.stardust.base.command.BaseCommand;

@BaseCommand("g")
public class GlobalCommand extends ChannelMessageCommand {

    public GlobalCommand(ChannelsPlugin plugin) {
		super(plugin, plugin.getGlobal(), true);
	}

}
