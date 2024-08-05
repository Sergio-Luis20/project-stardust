package net.stardust.base.utils.persistence;

import lombok.experimental.StandardException;

/**
 * Unchecked exception thrown when the serialization or deserialization
 * fails using a {@link DataManager}.
 * 
 * @see DataManager
 * 
 * @author Sergio Luis
 */
@StandardException
public class PersistenceSerializationException extends RuntimeException {

}
