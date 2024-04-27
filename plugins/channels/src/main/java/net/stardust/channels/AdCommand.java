package net.stardust.channels;

import net.stardust.base.command.BaseCommand;

@BaseCommand("ad")
public class AdCommand extends ChannelMessageCommand {

	public AdCommand(ChannelsPlugin plugin) {
		super(plugin, plugin.getAd(), false);
	}
    
}
