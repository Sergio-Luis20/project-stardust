package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.model.economy.transaction.operation.Operation;
import net.stardust.base.model.economy.transaction.operation.OperationFailedException;

/**
 * Represents a transaction being made between a buyer
 * and a seller. They can be everything that implements
 * those interfaces, no restrictions.
 * 
 * @see MonetaryEntity
 * @see Negotiators
 * 
 * @author Sergio Luis
 */
public interface Transaction {

    /**
     * Returns the total value being negotiated. "Zero"
     * means free. Should never be null.
     * 
     * @see MonetaryEntity
     * @return the money.
     */
    MonetaryEntity getValue();

    /**
     * Returns the pair buyer-seller of this transaction, with
     * the possible presence of the starter of the transaction.
     * 
     * @see Negotiators
     * @return the pair buyer-seller.
     */
    Negotiators getNegotiators();

    /**
     * Performs an {@link Operation} on this {@link Transaction} object.
     * 
     * @see Operation
     * @see OperationFailedException
     * @param operation the operation to be performed with this {@link Transaction} object
     * @throws OperationFailedException if the operation fails due to business rules
     */
    default void performOperation(Operation operation) throws OperationFailedException {
        operation.execute(this);
    }

    /**
     * Static factory to create a {@link Transaction} with the value being negotiated
     * (normally the price) and the {@link Negotiators} participating.
     * 
     * @see Transaction
     * @see Negoatiators
     * @see MonetaryEntity
     * @param value the value being negotiated
     * @param negotiators the negotiators
     * @return the {@link Transaction} instance with the passed arguments
     */
    static Transaction newTransaction(MonetaryEntity value, Negotiators negotiators) {
        return new Transaction() {
            
            @Override
            public MonetaryEntity getValue() {
                return value;
            }

            @Override
            public Negotiators getNegotiators() {
                return negotiators;
            }

        };
    }

}
