package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.MonetaryEntity;
import net.stardust.base.model.economy.transaction.operation.Operation;
import net.stardust.base.model.economy.transaction.operation.OperationFailedException;

/**
 * Represents a transaction being made between a buyer
 * and a seller. They can be everything that implements
 * those interfaces, no restrictions. More information
 * in the methods' docs.
 * 
 * @author Sergio Luis
 */
public interface Transaction {

    /**
     * Returns the total value being negotiated. "Zero"
     * means free. Should never be null.
     * @return the money.
     */
    MonetaryEntity getValue();

    /**
     * Returns the pair buyer-seller of this transaction, with
     * the possible presence of the starter of the transaction.
     * @return the pair buyer-seller.
     */
    Negotiators getNegotiators();

    default void performOperation(Operation operation) throws OperationFailedException {
        operation.execute(this);
    }

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
