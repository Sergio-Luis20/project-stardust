package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.transaction.Transaction;

/**
 * This operation checks if a buyer negotiator of a {@link Transaction}
 * has enough money to pay the seller in its wallet. If that condition
 * is not met, then a {@link OperationFailedException} is thrown.
 * 
 * @see OperationFailedException
 * 
 * @author Sergio Luis
 */
public class MoneyNode implements Operation {

    /**
     * Checks if the buyer has enough money to pay the seller. The minimum amount
     * necessary is the value returned by {@link Transaction#getValue()}. If the
     * buyer doesn't have at least that quantity, an {@link OperationFailedException}
     * is thrown.
     * 
     * @see MoneyNode
     * @see Transaction#getValue()
     * @param transaction the transaction being processed
     * @throws OperationFailedException if the buyer doesn't have enough money
     * to pay the seller
     */
    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var price = transaction.getValue();
        var buyerMoney = transaction.getNegotiators().getBuyer().getWallet().getMoney(price.getCurrency());
        if(!buyerMoney.isSubtractionPossible(price.getValue())) {
            throw OperationFailedException.fromKey("buyer-money", this);
        }
    }
    
}
