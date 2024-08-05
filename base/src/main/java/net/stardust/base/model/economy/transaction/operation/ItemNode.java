package net.stardust.base.model.economy.transaction.operation;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;

/**
 * This operation checks if the seller of an
 * {@link ItemTransaction} has the items being
 * negotiated inside its storage. If the {@link Transaction}
 * is not an implementation of {@link ItemTransaction},
 * then this operation just does nothing.
 * 
 * @see Transaction
 * @see ItemTransaction
 * 
 * @author Sergio Luis
 */
public class ItemNode implements Operation {

    /**
     * Checks if the seller has the items to sell to the buyer
     * inside its storage. If the {@link Transaction} is not an
     * {@link ItemTransaction}, this operation just does nothing.
     * 
     * @see Transaction
     * @see ItemTransaction
     * @param transaction the transaction to be processed
     * @throws OperationFailedException if the seller doesn't have the
     * items to sell to the buyer
     */
    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        if(transaction instanceof ItemTransaction itemTransaction) {
            Storage storage = itemTransaction.getNegotiators().getSeller().getStorage();
            boolean hasAll = true;
            for(ItemStack item : itemTransaction.getItems()) {
                hasAll &= storage.hasItem(item);
                if(!hasAll) break;
            }
            if(!hasAll) {
                throw OperationFailedException.fromKey("seller-item", this);
            }
        }
    }
    
}
