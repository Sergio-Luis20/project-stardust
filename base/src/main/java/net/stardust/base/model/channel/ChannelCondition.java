package net.stardust.base.model.channel;

import java.util.function.Predicate;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;

public interface ChannelCondition extends Predicate<CommandSender> {
    
    Component getNotAllowedMessage(CommandSender sender);

}
