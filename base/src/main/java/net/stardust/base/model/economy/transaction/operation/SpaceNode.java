package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;

// Checks if an ItemHolder has capacity to store seller's items.

/**
 * Operation that checks if the buyer of an {@link ItemTransaction} has
 * enough space (slots) in its {@link Storage} to store all the items
 * being negotiated. If that condition does not met, then an {@link OperationFailedException}
 * is thrown. If the {@link Transaction} object passed to {@link #execute(Transaction)}
 * is not an implementation of {@link ItemTransaction}, then nothing is done.
 * 
 * @see Operation
 * @see Transaction
 * @see ItemTransaction
 * @see Storage
 * @see OperationFailedException
 * @see #execute(Transaction)
 * 
 * @author Sergio Luis
 */
public class SpaceNode implements Operation {

    /**
     * Checks if a buyer of an {@link ItemTransaction} has enough
     * space in its {@link Storage} to store the items being negotiated.
     * If it has no enough space, an {@link OperationFailedException} is
     * thrown. If the {@link Transaction} object is not an {@link ItemTransaction},
     * nothing is done.
     * 
     * @see Transaction
     * @see ItemTransaction
     * @see Storage
     * @see OperationFailedException
     * @see SpaceNode
     * @param transaction the transaction object
     * @throws OperationFailedException if the buyer has no enough space to store
     * the items being negotiated
     */
    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        if(transaction instanceof ItemTransaction itemTransaction) {
            Storage storage = itemTransaction.getNegotiators().getBuyer().getStorage();
            if (!storage.canStore(itemTransaction.getItems())) {
                throw OperationFailedException.fromKey("space", this);
            }
        }
    }
    
}
