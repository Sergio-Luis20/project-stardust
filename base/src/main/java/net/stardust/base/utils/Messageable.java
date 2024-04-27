package net.stardust.base.utils;

import java.util.Arrays;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public interface Messageable {
    
    void sendMessage(Component component);
    
    default void sendMessage(String message) {
        sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    default void sendMessage(String... messages) {
        Arrays.asList(messages).forEach(this::sendMessage);
    }

    default void sendMessage(Component... components) {
        sendMessage(components);
    }

    default void sendMessage(Iterable<Component> messages) {
        messages.forEach(this::sendMessage);
    }

}
