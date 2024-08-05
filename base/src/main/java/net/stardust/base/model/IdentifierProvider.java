package net.stardust.base.model;

import java.io.Serializable;

/**
 * This interface represents an object that can
 * supply an identifier of any kind. This is util
 * for classes that can't extend some identifier class
 * and but don't need to rewrite internal code to
 * implement the same functionality of {@link Identifier}.
 * 
 * @see Identifier
 * 
 * @author Sergio Luis
 */
@FunctionalInterface
public interface IdentifierProvider<T extends Serializable> {
    
    /**
     * Returns the {@link Identifier} representing
     * the same functionality of this class.
     * 
     * @see Identifier
     * @return the {@link Identifier} object
     */
    Identifier<T> getIdentifier();

}
