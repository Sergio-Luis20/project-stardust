package net.stardust.base.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class AutomaticMessages {
    
    private AutomaticMessages() {}

    public static Component notFound(String key) {
        return build("not-found", NamedTextColor.RED, Component.translatable(key, NamedTextColor.RED));
    }

    public static Component teleportingTo(String key) {
        return build("teleporting-to", NamedTextColor.GREEN, Component.translatable(key, NamedTextColor.GOLD));
    }

    public static Component teleportingTo(Component... args) {
        return build("teleporting-to", NamedTextColor.GREEN, args);
    }

    public static Component negativePage() {
        return Component.translatable("pageable.negative-page", NamedTextColor.RED);
    }

    public static Component greaterPage() {
        return Component.translatable("pageable.greater-page", NamedTextColor.RED);
    }

    public static Component pageable(String subkey, Component page) {
        return Component.translatable("pageable." + subkey, NamedTextColor.GOLD, page);
    }

    public static Component internalServerError() {
        return Component.translatable("error.internal", NamedTextColor.RED);
    }

    private static Component build(String mainKey, TextColor mainColor, Component... args) {
        return Component.translatable(mainKey, mainColor, args);
    }

}
