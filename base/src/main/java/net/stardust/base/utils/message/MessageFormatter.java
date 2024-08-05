package net.stardust.base.utils.message;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Interface that represents an object able to format a {@link Component}
 * message into other. If you are going to do String manipulation, use
 * some {@link ComponentSerializer} to do that.
 * 
 * @see PlainTextComponentSerializer
 * @see LegacyComponentSerializer
 */
@FunctionalInterface
public interface MessageFormatter<T extends CommandSender> {
    
    /**
     * Returns the formatted message. Use {@link ComponentSerializer}s to do
     * String manipulations.
     * 
     * @see PlainTextComponentSerializer
     * @see LegacyComponentSerializer
     * @param sender the sender of the message
     * @param message the raw message to be formatted
     * @return the formatted message
     */
    Component formatMessage(T sender, Component message);

}
