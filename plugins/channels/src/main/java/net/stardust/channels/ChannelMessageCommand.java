package net.stardust.channels;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.stardust.base.command.AsyncCommand;
import net.stardust.base.command.CommandEntry;
import net.stardust.base.model.channel.Channel;
import net.stardust.base.utils.gameplay.MentionService;

public abstract class ChannelMessageCommand extends AsyncCommand<ChannelsPlugin> {

    private Channel channel;
    private MentionService service;
    private boolean mentionAllowed;

    public ChannelMessageCommand(ChannelsPlugin plugin, Channel channel, boolean mentionAllowed) {
		super(plugin);
        this.channel = Objects.requireNonNull(channel, "channel");
        this.mentionAllowed = mentionAllowed;
        service = MentionService.INSTANCE;
	}
    
    @CommandEntry
    public void execute(String message) {
        CommandSender sender = sender();
        if(sender instanceof Player player) {
            UUID playerId = uniqueId(player);
            if(!plugin.isPropertyActivated(playerId, channel.getClass().getName(), "status")) {
                messager.message(player, Component.translatable("channel.disabled-channel", NamedTextColor.RED));
                return;
            }
        }
        if(mentionAllowed) {
            service.mention(sender, message, channel);
        } else {
            channel.sendMessage(sender(), message);
        }
    }
    
}
