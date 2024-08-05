package net.stardust.base.utils.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.stardust.base.model.StardustEntity;

/**
 * Simple annotation to preserve the id class of a
 * {@link StardustEntity}, avoiding type erasure of
 * generic. Implementations of {@link StardustEntity}
 * must have this annotation, as specified in the
 * interface documentation.
 * 
 * @see StardustEntity
 * 
 * @author Sergio Luis
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseEntity {
    
    /**
     * The id type of this entity in database.
     * 
     * @return the id.
     */
    Class<?> value();

}
