package net.stardust.base.model.user;

import java.util.UUID;

import net.stardust.base.model.IdentifierProvider;

/**
 * Represents every class that can provide a
 * {@link PlayerIdentifier}. More details in
 * {@link IdentifierProvider}.
 * 
 * @see PlayerIdentifier
 * @see IdentifierProvider
 * 
 * @author Sergio Luis
 */
public interface PlayerIdentifierProvider extends IdentifierProvider<UUID> {
    
    /**
     * Returns the {@link PlayerIdentifier} object.
     * 
     * @see PlayerIdentifier
     * @see IdentifierProvider
     * @return the {@link PlayerIdentifier} object
     */
    PlayerIdentifier getIdentifier();

}
