package net.stardust.base;

import net.stardust.base.model.Identifier;
import net.stardust.base.model.user.PlayerIdentifier;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public final class Stardust {

    public static final String STARDUST_NAMESPACE = "stardust";

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

}
