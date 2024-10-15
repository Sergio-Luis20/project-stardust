package net.stardust.base.utils.persistence;

import lombok.experimental.StandardException;

/**
 * Exception thrown when a serialized object in a
 * {@link PersistentDataContainer}
 * does not follow a schema defined by a {@link KeyMapper}. See
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
