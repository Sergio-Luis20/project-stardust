package net.stardust.base.utils.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseEntity {
    
    /**
     * The id type of this entity in database.
     * @return the id.
     */
    Class<?> value();

}
