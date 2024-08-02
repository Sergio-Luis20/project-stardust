package net.stardust.base.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.command.CommandSender;

/**
 * This annotation specifies metadata to being passed
 * to command entries in a command class of Stardust
 * Command System. A command entry is no more than a method
 * annotated by this annotation, being able to be called
 * by the system if a sender queries a command that matches
 * the syntax of this entry. More details about syntax in
 * {@link CommandEntry#value()} documentation.
 * 
 * @see CommandEntry#value()
 * @author Sergio Luis
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandEntry {
	
	/**
	 * These are the subcommands of the command. For
	 * example, suppose a command named <b>foo</b> and it
	 * can be queried as <b>/foo faa fee fii</b>; also suppose
	 * that <b>fii</b> is a parameter. Then, <b>foo</b> is the
	 * name of the command, <b>faa</b> and <b>fee</b> are the
	 * subcommands and <b>fii</b> is a parameter. The String that
	 * should be passed into this value is <b>"faa fee"</b>. The
	 * syntax of every Stardust Command System command is
	 * {@code name of the command -> subcommands (if any) -> parameters (if any)}.
	 * 
	 * @return the String containing the subcommands of this command
	 */
	String value() default "";

	/**
	 * These are the CommandSender classes that are allowed to run
	 * this specific entry of the command. Note: if the annotation
	 * {@link BaseCommand} does not specify the classes passed into
	 * this array or at least superclasses of them, then the classes
	 * here are going to be ignored and not allowed to run this entry.
	 * Beware the inconsistencies between this annotation and {@link BaseCommand}
	 * because they can turn your entry non-executable.
	 * 
	 * @return the specific sender types that are allowed to run
	 * this entry
	 */
	Class<? extends CommandSender>[] types() default CommandSender.class;

	/**
	 * Returns if this entry can only be executed by an op sender. Note:
	 * if {@link BaseCommand#opOnly()} returns true, then this value will
	 * be ignored and even if it returns false, the entry will be able to
	 * only be executed by op senders.
	 * 
	 * @return true if this entry should be executed only by op senders,
	 * false otherwise
	 */
	boolean opOnly() default false;
	
	/**
	 * This value indicates if the command class should automatically
	 * show the default <b>no permission</b> message if the sender has
	 * no permision to execute this entry, either by being a sender
	 * which class is not present in {@link BaseCommand#types()} or
	 * not being op if necessary. You can turn this option to false if
	 * you want to check manually in the entry and send a custom message.
	 * 
	 * @return true if the command class should automatically send the
	 * default <b>no permission</b> message, false otherwise
	 */
	boolean showMessage() default true;

	/**
	 * Should return true if the annotated method
	 * has a String as last parameter and that String
	 * must be a single word.
	 * 
	 * @return true if the last String must be a single
	 * word, false otherwise
	 */
	boolean oneWordFinalString() default false;
	
}
