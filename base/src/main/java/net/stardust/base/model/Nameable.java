package net.stardust.base.model;

import java.util.Locale;
import java.util.ResourceBundle;

import net.stardust.base.utils.database.lang.Translation;

/**
 * This interface defines everything that can have a "name" and
 * that name can be translated based on a {@link Locale} object.
 * 
 * @see Locale
 * @see ResourceBundle
 * @see Translation
 * 
 * @author Sergio Luis
 */
public interface Nameable {
    
    /**
     * Returns the original name of this object. This
     * should not change.
     * 
     * @return the original name of this object.
     */
    String getName();

    /**
     * Returns the name of this object depending on the
     * Locale object passed as parameter. Can vary from locale
     * to locale, but must be consistent and not change for
     * every locale.
     * 
     * @see Locale
     * @see ResourceBundle
     * @see Translation
     * @param Locale the locale to view the translated name.
     * @return the translated name.
     */
    String getName(Locale locale);

}
