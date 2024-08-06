package net.stardust.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.user.PlayerIdentifier;
import net.stardust.base.utils.BatchList;
import net.stardust.base.utils.StardustThreads;
import net.stardust.base.utils.message.Messager;
import net.stardust.base.utils.plugin.PluginConfig;

/**
 * The Stardust class brings together utility methods for use in a general
 * context of the application.
 * 
 * @author Sergio Luis
 */
public final class Stardust {

    /**
     * The namespace of Stardust server.
     */
    public static final String STARDUST_NAMESPACE = "stardust";

    /**
     * The default number of elements in a page, normally used
     * with {@link BatchList} for pagination of elements.
     */
    public static final int DEFAULT_PAGE_SIZE = 5;

    private Stardust() {
    }

    /**
     * Creates a {@link NamespacedKey} instance using the
     * {@link #STARDUST_NAMESPACE}
     * value as namespace and the String parameter as key.
     * 
     * @see NamespacedKey
     * @see #STARDUST_NAMESPACE
     * @param key the key of the namespacedkey
     * @return the namespacedkey
     * @throws IllegalArgumentException if key is null or invalid (see
     *                                  {@link NamespacedKey}
     *                                  documentation about its syntax for this
     *                                  validation concept).
     */
    public static NamespacedKey stardust(String key) {
        return new NamespacedKey(STARDUST_NAMESPACE, key);
    }

    /**
     * Creates a new instance of an object by its fully qualified class name
     * and an array of objects to be passed to a public constructor present
     * in the class that accepts them. Note that the arguments in the array
     * are order-sensitive, they must be in the same order of the parameters
     * requested by the constructor.
     * 
     * @param className       the fully qualified class name
     * @param constructorArgs the arguments to be passed to a public constructor
     * @return a new instance of the class whose name was passed
     * @throws ClassNotFoundException    if no class was found for the name
     *                                   parameter
     * @throws NoSuchMethodException     if no constructor was found for that
     *                                   combination of arguments
     * @throws InvocationTargetException if the constructor throws an exception
     *                                   during execution;
     *                                   you can get the thrown exception by doing
     *                                   {@link InvocationTargetException#getTargetException()}
     * @throws InstantiationException    if the class can't be instantiated, for
     *                                   example for being
     *                                   abstract, an interface, a primitive type,
     *                                   an array etc...
     * @throws IllegalAccessException    if the constructor is not public
     */
    public static Object newInstance(String className, Object... constructorArgs) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getConstructor(constructorArgs == null ? null
                : Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class<?>[]::new));
        return constructor.newInstance(constructorArgs);
    }

    /**
     * Works the same way as {@link #newInstance(String, Object...)}, but tries to
     * access the constructor even if it is not public.
     * 
     * @param className       the fully qualified class name
     * @param constructorArgs the arguments to be passed to a public constructor
     * @return a new instance of the class whose name was passed
     * @throws ClassNotFoundException    if no class was found for the name
     *                                   parameter
     * @throws NoSuchMethodException     if no constructor was found for that
     *                                   combination of arguments
     * @throws InvocationTargetException if the constructor throws an exception
     *                                   during execution;
     *                                   you can get the thrown exception by doing
     *                                   {@link InvocationTargetException#getTargetException()}
     * @throws InstantiationException    if the class can't be instantiated, for
     *                                   example for being
     *                                   abstract, an interface, a primitive type,
     *                                   an array etc...
     * @throws IllegalAccessException    if this method fails to access the
     *                                   non-public constructor
     */
    public static Object newPrivateInstance(String className, Object... constructorArgs) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getDeclaredConstructor(constructorArgs == null ? null
                : Arrays.stream(constructorArgs).map(Object::getClass).toArray(Class<?>[]::new));
        return constructor.newInstance(constructorArgs);
    }

    /**
     * Returns an {@link Identifier} object for a passed {@link CommandSender}.
     * 
     * @implNote If sender is a player, returns a {@link PlayerIdentifier},
     *           otherwise
     *           any other type of {@link CommandSender} implementation is treated
     *           as a Server
     *           sender, so {@link ServerIdentifier#INSTANCE} is returned.
     * 
     * @see CommandSender
     * @see Identifier
     * @see PlayerIdentifier
     * @see ServerIdentifier
     * @see ServerIdentifier#INSTANCE
     * @param sender the command sender to create a new identifier
     * @return the identifier of the sender
     * @throws NullPointerException if sender is null
     */
    public static Identifier<?> getIdentifier(CommandSender sender) {
        Objects.requireNonNull(sender, "sender");
        return sender instanceof Player player ? new PlayerIdentifier(player) : ServerIdentifier.INSTANCE;
    }

    /**
     * Creates a new {@link NamespacedKey} instance from a String that follows its
     * syntax.
     * A {@link NamespacedKey} is made of two elements, those are, the namespace and
     * the key (oh no).
     * The basic syntax is <b>namespace:key</b>, and there are some rules for
     * that String:
     * <ul>
     * <li>The String cannot be empty
     * <li>The String length must be less than 256 characters;
     * <li>The String must contain only one "<b>:</b>" (colon - U+003A), the one
     * that
     * separates namespace
     * from key;
     * <li>The String namespace must match regex <b>[a-z0-9._-]</b>, that is,
     * contain
     * only low case
     * a-z characters without accents or marks, digits from 0 to 9 (decimal), the
     * dot, the underline
     * and the hyphen-minus;
     * <li>The String key must match regex <b>[a-z0-9/._-]</b>, that is, contain
     * only low
     * case a-z
     * characters without accents or marks, digits from 0 to 9 (decimal), the
     * fowardslash (also
     * called just slash or solidus), the dot, the underline and the hyphen-minus.
     * </ul>
     * If any of those conditions do not met, an {@link IllegalArgumentException} is
     * thrown.
     * This String syntax is the same followed by the one returned by
     * {@link NamespacedKey#asString()},
     * as that is the "String representation" that this method reverse parses.
     * 
     * <p>
     * Examples:
     * 
     * <pre>
     * <code>
     * NamespacedKey nsk = Stardust.key("stardust:something_crazy");
     * NamespacedKey mineNsk = Stardust.key("creeper"); // minecraft:creeper
     * </code>
     * </pre>
     * 
     * @implNote This method interpretes that the passed parameter is the String
     *           representation
     *           of a {@link NamespacedKey}, and by representation we can also say
     *           "equivalence", so for a
     *           null String parameter, a null {@link NamespacedKey} will be
     *           returned.
     * @implNote If the String parameter does not contain a colon, then this method
     *           treates the
     *           parameter no more as a whole {@link NamespacedKey} String
     *           representation, but only the
     *           key of it, and takes {@link NamespacedKey#MINECRAFT} as the
     *           namespace.
     * 
     * @see NamespacedKey
     * @see NamespacedKey#asString()
     * @see NamespacedKey#MINECRAFT
     * @param str the String representation of a {@link NamespacedKey}, such as
     *            returned by
     *            {@link NamespacedKey#asString()}.
     * @return the {@link NamespacedKey} object parsed from the String parameter
     * @throws IllegalArgumentException if the String is empty, its length is 256 or
     *                                  more, contains more than one colon, the
     *                                  namespace does not match its regex or the
     *                                  key does not match its regex
     */
    public static NamespacedKey key(String str) {
        if (str == null)
            return null;
        int len = str.length();
        if (len == 0 || len >= 256) {
            throw new IllegalArgumentException("NamespacedKey must have length greater than 0 and smaller than 256");
        }
        String namespace, key;
        if (!str.contains(":")) {
            namespace = NamespacedKey.MINECRAFT;
            key = str;
        } else {
            String[] split = str.split(":");
            if (split.length > 2)
                throw new IllegalArgumentException("The String must be in format 'namespace:key'. String:" + str);
            namespace = split[0];
            key = split[1];
            if (!namespace.matches("[a-z0-9._-]+"))
                throw new IllegalArgumentException("Invalid namespace. Must be [a-z0-9._-]: " + namespace);
            if (!key.matches("[a-z0-9/._-]+"))
                throw new IllegalArgumentException("Invalid key. Must be [a-z0-9/._-]: " + key);
        }
        return new NamespacedKey(namespace, key);
    }

    /**
     * Works the same way as
     * {@link #listPageableString(CommandSender, int, Stream, String, Function)}.
     * This method just call that one passing {@link Collection#stream()} as
     * argument.
     * 
     * @param <T>             the type of the elements that will be converted to
     *                        {@link String} messages, and then to {@link Component}
     *                        ones
     * @param sender          the {@link CommandSender} to send messages
     * @param page            the number of the requested page (starts from 1)
     * @param elements        the {@link Collection} of <b>all</b> elements that are
     *                        going to be paged and converted to messages
     * @param pageableListKey the translation key of the element type name to make
     *                        the header message
     * @param toMessage       the function that converts elements of the type <T> to
     *                        {@link String}s to then be converted to
     *                        {@link Component}s;
     *                        it is recommended this function to
     *                        absolutely never return null
     * @throws NullPointerException
     */
    public static <T> void listPageableString(CommandSender sender, int page, Collection<T> elements,
            String pageableListKey, Function<T, String> toMessage) {
        listPageableString(sender, page, elements.stream(), pageableListKey, toMessage);
    }

    /**
     * Works the same way as
     * {@link #listPageable(CommandSender, int, Stream, String, Function)},
     * but the function passed to that method is a new function that converts the
     * String obtained
     * by this function parameter into a {@link Component}. The function in question
     * is this:
     * 
     * <pre>
     * <code>
     * element -> Component.text("» " + toMessage.apply(element), 
     *          NamedTextColor.GREEN, TextDecoration.ITALIC)
     * </code>
     * </pre>
     * 
     * @param <T>             the type of the elements that will be converted to
     *                        {@link String} messages, and then to {@link Component}
     *                        ones
     * @param sender          the {@link CommandSender} to send messages
     * @param page            the number of the requested page (starts from 1)
     * @param elements        the {@link Stream} of <b>all</b> elements that are
     *                        going to be paged and converted to messages
     * @param pageableListKey the translation key of the element type name to make
     *                        the header message
     * @param toMessage       the function that converts elements of the type <T> to
     *                        {@link String}s to then be converted to
     *                        {@link Component}s;
     *                        it is recommended this function to
     *                        absolutely never return null
     * @throws NullPointerException if any parameter is null
     */
    public static <T> void listPageableString(CommandSender sender, int page, Stream<T> elements,
            String pageableListKey, Function<T, String> toMessage) {
        listPageable(sender, page, elements, pageableListKey,
                element -> Component.text("» " + toMessage.apply(element), NamedTextColor.GREEN,
                        TextDecoration.ITALIC));
    }

    /**
     * Works the same way as specified in
     * {@link #listPageable(CommandSender, int, Stream, String, Function)}.
     * This method just call that one passing {@link Collection#stream()} as
     * argument.
     * 
     * @see #listPageable(CommandSender, int, Stream, String, Function)
     * @see #listPageableString(CommandSender, int, Collection, String, Function)
     * @see #listPageableString(CommandSender, int, Stream, String, Function)
     * @param <T>             the type of the elements that will be converted to
     *                        {@link Component} messages
     * @param sender          the {@link CommandSender} to send messages
     * @param page            the number of the requested page (starts from 1)
     * @param elements        the {@link Collection} of <b>all</b> elements that are
     *                        going to be paged and converted to messages
     * @param pageableListKey the translation key of the element type name to make
     *                        the header message
     * @param toMessage       the function that converts elements of the type <T> to
     *                        {@link Component}s; it is recommended this function to
     *                        absolutely never return null
     * @throws NullPointerException if any parameter is null
     */
    public static <T> void listPageable(CommandSender sender, int page, Collection<T> elements,
            String pageableListKey, Function<T, Component> toMessage) {
        listPageable(sender, page, elements.stream(), pageableListKey, toMessage);
    }

    /**
     * <p>
     * Sends a big message to a {@link CommandSender}. That message is made of
     * various
     * elements of a page and a header indicating what page it is and what kind of
     * elements are in the page.
     * </p>
     * 
     * <p>
     * The pages are obtained from a {@link Stream} of elements, collecting them
     * into a
     * {@link BatchList} and passing a default value as the batch size
     * ({@link Stardust#DEFAULT_PAGE_SIZE}).
     * Each batch of the batch list is considered a page; then this method gets the
     * page (batch)
     * requested by the caller using the <b>page</b> parameter. Finally, it converts
     * every
     * element of the type T in the batch to a {@link Component} using the function
     * parameter,
     * making them suitable for being sent to the {@link CommandSender} parameter.
     * </p>
     * 
     * <p>
     * No parameter is allowed to be null, and the page number initiates count from
     * 1,
     * not 0; it will be decreased by 1 internally to get the batch index. If the
     * page index
     * is out of the range, the error messages will be sent to the
     * {@link CommandSender}, no
     * exception will be thrown.
     * </p>
     * 
     * <p>
     * The purpose of this method is just allowing the {@link CommandSender} to
     * choose
     * a page of elements to view by text.
     * </p>
     * 
     * @implNote This method ensures that messages will be sent in Bukkit Main
     *           thread,
     *           so it is thread-safe in that context.
     * 
     * @see #listPageable(CommandSender, int, Collection, String, Function)
     * @see #listPageableString(CommandSender, int, Collection, String, Function)
     * @see #listPageableString(CommandSender, int, Stream, String, Function)
     * @see #DEFAULT_PAGE_SIZE
     * @param <T>             the type of the elements that will be converted to
     *                        {@link Component} messages
     * @param sender          the {@link CommandSender} to send messages
     * @param page            the number of the requested page (starts from 1)
     * @param elements        the {@link Stream} of <b>all</b> elements that are
     *                        going to be paged and
     *                        converted to messages
     * @param pageableListKey the translation key of the element type name to make
     *                        the header message
     * @param toMessage       the function that converts elements of the type <T> to
     *                        {@link Component}s; it
     *                        is recommended this function to absolutely never
     *                        return null
     * @throws NullPointerException if any parameter is null
     */
    public static <T> void listPageable(CommandSender sender, int page, Stream<T> elements,
            String pageableListKey, Function<T, Component> toMessage) {
        notNull(sender, elements, pageableListKey, toMessage);
        Messager messager = PluginConfig.get().getPlugin().getMessager();
        int index = page - 1;
        if (index < 0) {
            messager.message(sender, Component.translatable("pageable.negative-page", NamedTextColor.RED));
            return;
        }
        BatchList<T> batchList = elements.collect(BatchList.collector(DEFAULT_PAGE_SIZE));
        List<T> batch;
        try {
            batch = batchList.getBatch(index);
        } catch (IndexOutOfBoundsException e) {
            messager.message(sender, Component.translatable("pageable.greater-page", NamedTextColor.RED));
            return;
        }
        Component pages = Component.text("(p. " + page + "/" + batchList.getTotalBatches() + ")", NamedTextColor.GOLD);
        Component header = Component.translatable("pageable.list." + pageableListKey, NamedTextColor.GOLD, pages);
        messager.message(sender, header);
        StardustThreads.run(() -> {
            for (T element : batch) {
                sender.sendMessage(toMessage.apply(element));
            }
        });
    }

    /**
     * Utility method for nullity checking of multiple objects at once.
     * If any of the objects are null, a {@link NullPointerException} is
     * thrown.
     * 
     * @see #notNull(String, Object...)
     * @param objects the objects to check nullity
     * @throws NullPointerException if any object is null
     */
    public static void notNull(Object... objects) {
        notNull("Argument must not be null", objects);
    }

    /**
     * Utility method for nullity checking of multiple objects at once.
     * If any of the objects are null, a {@link NullPointerException} is
     * thrown with the String message parameter.
     * 
     * @see #notNull(Object...)
     * @param message the message of the {@link NullPointerException}, if thrown
     * @param objects the objects to check nullity
     * @throws NullPointerException if any object is null
     */
    public static void notNull(String message, Object... objects) {
        for (Object obj : objects) {
            if (obj == null) {
                throw new NullArgumentException(message);
            }
        }
    }

}
