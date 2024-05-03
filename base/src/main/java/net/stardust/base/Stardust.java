package net.stardust.base;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.BatchList;
import net.stardust.base.utils.Messager;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.plugin.PluginConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Stardust {

    public static final String STARDUST_NAMESPACE = "stardust";
    public static final int DEFAULT_PAGE_SIZE = 5;

    private Stardust() {}

    public static NamespacedKey stardust(String key) {
        return new NamespacedKey(STARDUST_NAMESPACE, key);
    }

    public static Object newInstance(String className, Object... constructorArgs) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getConstructor(constructorArgs == null ? null
                : Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class<?>[]::new));
        return constructor.newInstance(constructorArgs);
    }

    public static Object newPrivateInstance(String className, Object... constructorArgs) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor(constructorArgs == null ? null
                : Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class<?>[]::new));
        return constructor.newInstance(constructorArgs);
    }

    public static Identifier<?> getIdentifier(CommandSender sender) {
        Objects.requireNonNull(sender, "sender");
        return sender instanceof Player player ? new PlayerIdentifier(player) : ServerIdentifier.INSTANCE;
    }

    public static <T> void listPageableString(CommandSender sender, int page, Collection<T> elements,
                                        String pageableListKey, Function<T, String> toMessage) {
        listPageableString(sender, page, elements.stream(), pageableListKey, toMessage);
    }

    public static <T> void listPageableString(CommandSender sender, int page, Stream<T> elements,
                                              String pageableListKey, Function<T, String> toMessage) {
        listPageable(sender, page, elements, pageableListKey,
                element -> Component.text("» " + toMessage.apply(element), NamedTextColor.GREEN,
                        TextDecoration.ITALIC));
    }

    public static <T> void listPageable(CommandSender sender, int page, Collection<T> elements,
                                        String pageableListKey, Function<T, Component> toMessage) {
        listPageable(sender, page, elements.stream(), pageableListKey, toMessage);
    }

    public static <T> void listPageable(CommandSender sender, int page, Stream<T> elements,
                                        String pageableListKey, Function<T, Component> toMessage) {
        BasePlugin plugin = PluginConfig.get().getPlugin();
        Messager messager = plugin.getMessager();
        int index = page - 1;
        if(index < 0) {
            messager.message(sender, Component.translatable("pageable.negative-page", NamedTextColor.RED));
            return;
        }
        BatchList<T> batchList = elements.collect(BatchList.collector(DEFAULT_PAGE_SIZE));
        List<T> batch;
        try {
            batch = batchList.getBatch(index);
        } catch(IndexOutOfBoundsException e) {
            messager.message(sender, Component.translatable("pageable.greater-page", NamedTextColor.RED));
            return;
        }
        Component pages = Component.text("(p. " + page + "/" + batchList.getTotalBatches() + ")", NamedTextColor.GOLD);
        Component header = Component.translatable("pageable.list." + pageableListKey, NamedTextColor.GOLD, pages);
        messager.message(sender, header);
        for(T element : batch) {
            messager.message(sender, StardustThreads.call(plugin, () -> toMessage.apply(element)));
        }
    }

}
