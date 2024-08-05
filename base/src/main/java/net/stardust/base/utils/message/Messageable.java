package net.stardust.base.utils.message;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Interface for simplifying the process of sending messages
 * to whatever thing that can receive them. This can be used
 * to encapsulate message sending without having to implement
 * {@link CommandSender} for example and avoid having to implement
 * methods that you will not use, as said in Interface Segregation
 * Principle of SOLID.
 * 
 * @see Messager
 * 
 * @author Sergio Luis
 */
public interface Messageable {
    
    /**
     * Sends a {@link Component} message.
     * 
     * @param component the message
     */
    void sendMessage(Component component);
    
    /**
     * Sends a String message. The default implementation
     * uses {@link LegacyComponentSerializer#legacySection()} to
     * deserialize the message into a {@link Component}, so be
     * careful with Minecraft section syntax color.
     * 
     * @see LegacyComponentSerializer
     * @see LegacyComponentSerializer#legacySection()
     * @param message the message
     */
    default void sendMessage(String message) {
        sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    /**
     * Sends various messages as String. This method calls
     * {@link #sendMessage(String)} to every message in the
     * array, so check its documentation for important default
     * implementation details.
     * 
     * @see #sendMessage(String)
     * @param messages the messages
     */
    default void sendMessage(String... messages) {
        Arrays.asList(messages).forEach(this::sendMessage);
    }

    /**
     * Sends various messages as {@link Component}.
     * 
     * @param components the messages
     */
    default void sendMessage(Component... components) {
        sendMessage(Arrays.asList(components));
    }

    /**
     * Sends various messages in an {@link Iterable} of {@link Component}s.
     * 
     * @param messages the iterable of components
     */
    default void sendMessage(Iterable<Component> messages) {
        messages.forEach(this::sendMessage);
    }

}
