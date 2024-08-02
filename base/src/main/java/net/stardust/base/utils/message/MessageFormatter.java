package net.stardust.base.utils.message;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface MessageFormatter<T extends CommandSender> {
    
    Component formatMessage(T sender, Component message);

}
