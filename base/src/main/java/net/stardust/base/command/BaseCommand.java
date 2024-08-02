package net.stardust.base.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This annotation contains information for automating
 * command registration and execution in Stardust
 * Command System. It allows the command class (which
 * must be a subtype of {@link StardustCommand}) to be
 * scanned and registered and supply some execution
 * metadata.
 * 
 * All Stardust Command System classes must be annotated
 * with this annotation.
 * 
 * @author Sergio Luis
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseCommand {
    
    /**
     * This is the name of the command. It's the same
     * used in {@code plugin.yml} and in {@link JavaPlugin#getCommand(String)}.
     * 
     * @return the name of the command
     */
    String value();

    /**
     * This is the translation key for a translated usage
     * message for the user.
     * 
     * @return the translation key for usage message
     */
    String usageKey() default "";

    /**
     * These are the CommandSender classes that are allowed
     * to execute this command.
     * 
     * @return the allowed sender types
     */
    Class<? extends CommandSender>[] types() default CommandSender.class;

    /**
     * Should return true if this command must be executed
     * only by op senders.
     * 
     * @return true if the command can only be executed by
     * op senders, false otherwise
     */
    boolean opOnly() default false;

}
