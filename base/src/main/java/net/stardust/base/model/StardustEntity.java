package net.stardust.base.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import net.stardust.base.database.BaseEntity;

/**
 * Represents a database entity of Stardust server.
 * 
 * <p>
 * Not every class annotated with {@link Entity} must implement
 * this interface, but every class that implements this interface must
 * be annotated with {@link BaseEntity}. The generic parameter is the id class
 * of the entity in database (normally the primary key in relational model).
 * </p>
 * 
 * <p>
 * This interface extends {@link Serializable}, so implementations
 * must properly follow its contract and must deal with transient attributes
 * and custom serialization when necessary.
 * </p>
 * 
 * <p>
 * If this class is annotated with {@link Entity}, then the id returned
 * by {@link #getEntityId()} must be attribute annotated with {@link Id}.
 * </p>
 * 
 * <p>
 * The implementation class should have a public constructor with no
 * parameters to facilitate instantiation and the id attribute should not
 * be final.
 * </p>
 * 
 * More details in {@link #getEntityId()}
 * 
 * @see Id
 * @see Entity
 * @see BaseEntity
 * @see Serializable
 * @see #getEntityId()
 * 
 * @author Sergio Luis
 */
public interface StardustEntity<T> extends Serializable {

    /**
     * Returns the entity id in database (normally the primary key
     * in relation model).
     * 
     * <p>
     * It is recommended this id to be a primitive type, a wrapper type,
     * {@link String}, {@link BigInteger}, {@link BigDecimal}, {@link UUID} or any
     * other kind of object that implements {@link Serializable} and overrides
     * {@link Object#equals(Object)} and {@link Object#hashCode()}.
     * </p>
     * 
     * <p>
     * If you are in the last recommended case, then it is also recommended the
     * id to be unmodifiable and sufficient "anti-collision" like {@link UUID}
     * for example.
     * </p>
     * 
     * The id returned by this method should absolutely never be null.
     * 
     * @see BaseEntity
     * @see Serializable
     * @return the entity id
     */
    T getEntityId();

}
