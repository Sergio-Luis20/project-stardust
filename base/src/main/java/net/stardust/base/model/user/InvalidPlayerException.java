package net.stardust.base.model.user;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.experimental.StandardException;

/**
 * Exception thrown in contexts where a {@link Player}
 * is offline or does not exist.
 * 
 * @see Player
 * @see Bukkit#getPlayer(String)
 * @see Bukkit#getPlayer(java.util.UUID)
 * @see PlayerIdentifier
 * @see InvalidPlayerExceptionBuilder
 * 
 * @author Sergio Luis
 */
@StandardException
public class InvalidPlayerException extends RuntimeException {
    
    private UUID id;
    private String name;

    /**
     * Returns the player id. Can be null.
     * 
     * @return the player id
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the player id. Can be null.
     * 
     * @param id the player id
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the player name. Can be null.
     * 
     * @return the player name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the player name. Can be null.
     * 
     * @param name the player name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a new {@link InvalidPlayerExceptionBuilder} for building
     * a standard {@link InvalidPlayerException}.
     * 
     * @see InvalidPlayerException
     * @see InvalidPlayerExceptionBuilder
     * @return a new builder
     */
    public static InvalidPlayerExceptionBuilder builder() {
        return new InvalidPlayerExceptionBuilder();
    }

    /**
     * Simple builder for {@link InvalidPlayerException}.
     * 
     * @see InvalidPlayerException
     * 
     * @author Sergio Luis
     */
    public static class InvalidPlayerExceptionBuilder {

        private String message;
        private Throwable cause;
        private UUID id;
        private String name;

        /**
         * Sets the player name. Can be null.
         * 
         * @param name the player name
         * @return this builder
         */
        public InvalidPlayerExceptionBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the exception cause. Can be null.
         * 
         * @param cause the cause
         * @return this builder
         */
        public InvalidPlayerExceptionBuilder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Sets the player id. Can be null.
         * 
         * @param id the player id
         * @return this builder
         */
        public InvalidPlayerExceptionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the player name. Can be null.
         * 
         * @param name the player id
         * @return this builder
         */
        public InvalidPlayerExceptionBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds a new {@link InvalidPlayerException} with the
         * parameters passed via building methods.
         * 
         * @see InvalidPlayerException
         * @return a new {@link InvalidPlayerException} instance
         */
        public InvalidPlayerException build() {
            var exception = new InvalidPlayerException(message, cause);
            exception.setId(id);
            exception.setName(name);
            return exception;
        }

    }

}
