package net.stardust.base.model;

import lombok.experimental.StandardException;

/**
 * Exception thrown when an {@link Identifier} is of a not
 * acceptable of invalid type.
 * 
 * @see Identifier
 * 
 * @author Sergio Luis
 */
@StandardException
public class IllegalIdentifierException extends RuntimeException {

    private Identifier<?> identifier;

    /**
     * Creates an {@link IllegalIdentifierException} passing the
     * illegal {@link Identifier} as parameter. The parameter can
     * be null.
     * 
     * @see Identifier
     * @param identifier the illegal identifier
     */
    public IllegalIdentifierException(Identifier<?> identifier) {
        super(identifier != null ? "id: " + identifier.getId() : null);
        this.identifier = identifier;
    }

    /**
     * Returns the illegal {@link Identifier}.
     * 
     * @see Identifier
     * @return the illegal identifier
     */
    public Identifier<?> getIdentifier() {
        return identifier;
    }

    /**
     * Sets the illegal identifier. Can be null.
     * 
     * @param identifier the illegal identifier.
     */
    public void setIdentifier(Identifier<?> identifier) {
        this.identifier = identifier;
    }

}
