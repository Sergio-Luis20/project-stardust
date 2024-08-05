package net.stardust.base.model.economy;

import java.math.BigInteger;

import net.stardust.base.model.economy.wallet.Currency;

/**
 * Represents an object that can carry monetary value in
 * Stardust server.
 * 
 * @see Currency
 * @see BigInteger
 * 
 * @author Sergio Luis
 */
public interface MonetaryEntity extends Comparable<MonetaryEntity> {
    
    /**
     * Returns the currency of this entity. Should never be null.
     * Check {@link Currency} for more details.
     * 
     * @see Currency
     * 
     * @return the currency of this entity
     */
    Currency getCurrency();

    /**
     * The numeric value of this entity. Should never be null or negative.
     * 
     * @see BigInteger
     * 
     * @return the numeric value of this entity
     */
    BigInteger getValue();

    /**
     * Checks if a certain value can be subtracted of this entity
     * with it staying non-negative. Do not override this method unless
     * there is a very good reason.
     * 
     * @see BigInteger
     * @param value the value to be checked for subtraction
     * @return true if the passed value can be subtracted of
     * this entity without it remaining negative, false otherwise
     */
    default boolean isSubtractionPossible(BigInteger value) {
        return getValue().subtract(value).signum() >= 0;
    }

    /**
     * Default {@link Comparable#compareTo(Object)} to every MonetaryEntity.
     * Do not override this method unless there is a very good reason. If you
     * want to know how exactly comparation works, check {@link Comparable} class.
     * 
     * @see MonetaryEntity
     * @see Comparable
     * @see Comparable#compareTo(Object)
     * @param monetaryEntity the entity to be compared to
     * @return the result of the comparation
     */
    default int compareTo(MonetaryEntity monetaryEntity) {
        int compareOrder = getCurrency().getOrder() - monetaryEntity.getCurrency().getOrder();
        return compareOrder == 0 ? getValue().compareTo(monetaryEntity.getValue()) : compareOrder;
    }

}
