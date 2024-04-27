package net.stardust.base.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.StandardException;

@Getter
@Setter
@StandardException
public class IllegalIdentifierException extends RuntimeException {
    
    private Identifier<?> identifier;

    public IllegalIdentifierException(Identifier<?> identifier) {
        super(identifier != null ? "id: " + identifier.getId() : null);
        this.identifier = identifier;
    }

}
