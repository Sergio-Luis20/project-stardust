package net.stardust.base.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandEntry {
	
	// SubCommands
	String value() default "";
	SenderType[] types() default SenderType.ALL;
	boolean opOnly() default false;
	// No permission message
	boolean showMessage() default true;
	boolean oneWordFinalString() default false;
	
}
