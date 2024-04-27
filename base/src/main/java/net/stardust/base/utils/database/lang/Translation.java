package net.stardust.base.utils.database.lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.Throwables;
import net.stardust.base.utils.plugin.PluginConfig;

public final class Translation {

    public static final String DEFAULT_BUNDLE_PATH = "plugins/repository/bundles";
    public static final List<String> SUPPORTED_LANGUAGES;

    private static volatile boolean loaded;

    private Translation() {}

    public static synchronized void load() {
        if(!loaded) {
            var registry = TranslationRegistry.create(Key.key("stardust", "translation"));
            registry.defaultLocale(defaultLocale());
            SUPPORTED_LANGUAGES.stream().map(Translation::getBundle)
                .forEach(bundle -> registry.registerAll(bundle.getLocale(), bundle, true));
            GlobalTranslator.translator().addSource(registry);
            loaded = true;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static Component get(CommandSender sender, String key, Object... args) {
        return sender instanceof Player player ? get(player, key, args) : console(key, args);
    }

    public static Component get(Player player, String key, Object... args) {
        return get(StardustThreads.call(PluginConfig.get().getPlugin(), player::locale), key, args);
    }
    
    public static Component get(Locale locale, String key, Object... args) {
        return StardustThreads.call(PluginConfig.get().getPlugin(), 
            () -> MiniMessage.miniMessage().deserialize(string(locale, key, args)));
    }

    public static Component getTextComponent(CommandSender sender, String key, Object... args) {
        return sender instanceof Player player ? getTextComponent(player, key, args) : consoleText(key, args);
    }

    public static Component getTextComponent(Player player, String key, Object... args) {
        return get(StardustThreads.call(PluginConfig.get().getPlugin(), player::locale), key, args);
    }

    public static Component getTextComponent(Locale locale, String key, Object... args) {
        return StardustThreads.call(PluginConfig.get().getPlugin(), () -> Component.text(string(locale, key, args)));
    }

    public static Component consoleText(String key, Object... args) {
        return getTextComponent(consoleLocale(), key, args);
    }

    public static Component console(String key, Object... args) {
        return get(consoleLocale(), key, args);
    }

    public static String consoleString(String key, Object... args) {
        return string(consoleLocale(), key, args);
    }

    public static String string(CommandSender sender, String key, Object... args) {
        return sender instanceof Player player ? string(player, key, args) : consoleString(key, args);
    }

    public static String string(Player player, String key, Object... args) {
        return string(StardustThreads.call(PluginConfig.get().getPlugin(), player::locale), key, args);
    }

    public static String string(Locale locale, String key, Object... args) {
        return StardustThreads.call(PluginConfig.get().getPlugin(), 
            () -> GlobalTranslator.translator().translate(key, locale).format(args));
    }

    public static Locale defaultLocale() {
        return Locale.forLanguageTag("en");
    }

    public static Locale consoleLocale() {
        return Locale.forLanguageTag("pt");
    }

    private static ResourceBundle getBundle(String lang) {
        try {
            File dir = new File(DEFAULT_BUNDLE_PATH);
            if(!dir.exists()) {
                throw new BundleLoadException("Bundle directory does not exist");
            }
            URL[] urls = new URL[] { dir.toURI().toURL() };
            try(URLClassLoader loader = new URLClassLoader(urls)) {
                Locale locale = lang == null ? defaultLocale() : Locale.forLanguageTag(lang);
                return ResourceBundle.getBundle("lang", locale, loader, YamlResourceBundleControl.get());
            }
        } catch(Exception e) {
            Throwables.send(e);
            throw new BundleLoadException("could not load bundles for lang \"" + lang + "\""
                + " in the path \"" + DEFAULT_BUNDLE_PATH + "\"", e);
        }
    }
    
    static {
        String path = new File(DEFAULT_BUNDLE_PATH).getParentFile().getPath();
        Path pathObj = Paths.get(path, "supported-languages.txt");
        try {
            SUPPORTED_LANGUAGES = Collections.unmodifiableList(Files
                .readAllLines(pathObj, StandardCharsets.UTF_8));
        } catch(IOException e) {
            Throwables.send(e);
            throw new BundleLoadException("failed to read supported languages list", e);
        }
    }

}
