package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;

// Checks if an ItemHolder has capacity to store seller's items.
public class SpaceNode implements Operation {

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        if(transaction instanceof ItemTransaction itemTransaction) {
            Storage storage = itemTransaction.getNegotiators().getBuyer().getStorage();
            int capacity = storage.remainingCapacity();
            if(capacity != -1 && capacity < itemTransaction.getItems().size()) {
                throw OperationFailedException.fromKey("space");
            }
        }
    }
    
}
