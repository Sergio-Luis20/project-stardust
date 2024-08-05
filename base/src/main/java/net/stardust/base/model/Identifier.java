package net.stardust.base.model;

import java.io.Serializable;

import net.kyori.adventure.text.Component;

/**
 * This interface represents a simple id for whatever type
 * of entity. The identifier must have 3 ways to express its
 * value: the raw and effective id, a String format and a
 * {@link Component} format.
 * 
 * @see Component
 * 
 * @author Sergio Luis
 */
public interface Identifier<T extends Serializable> extends Serializable {
    
    /**
     * Returns the raw id. Should never be null.
     * 
     * @see #getStringName()
     * @see #getComponentName()
     * @return the raw id
     */
    T getId();

    /**
     * Returns the id in String format. Should never be null.
     * 
     * @see #getId()
     * @see #getComponentName()
     * @return the id in String format
     */
    String getStringName();

    /**
     * Returns the id in {@link Component} format. Should never be null.
     * 
     * @see Component
     * @see #getId()
     * @see #getStringName()
     * @return the id in {@link Component} format
     */
    Component getComponentName();

}
