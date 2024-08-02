package net.stardust.base.model.economy.transaction.operation;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;

// Checks if the seller has items to sell.
public class ItemNode implements Operation {

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
                throw OperationFailedException.fromKey("seller-item", getClass());
            }
        }
    }
    
}
