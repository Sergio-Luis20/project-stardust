package net.stardust.base.utils.persistence;

import lombok.experimental.StandardException;

/**
 * Exception thrown when a serialized object into a
 * {@link PersistentDataContainer}
 * does not follows a schema defined by a {@link KeyMapper}. Se
 * {@link KeyMapper}
 * documentation for more details.
 * 
 * @see KeyMapper
 * 
 * @author Sergio Luis
 */
@StandardException
public class NonRepresentativeDataException extends Exception {

}
