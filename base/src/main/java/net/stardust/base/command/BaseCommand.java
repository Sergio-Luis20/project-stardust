package net.stardust.base.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseCommand {
    
    // Name of the command
    String value();
    SenderType[] types() default SenderType.ALL;
    boolean opOnly() default false;

}
