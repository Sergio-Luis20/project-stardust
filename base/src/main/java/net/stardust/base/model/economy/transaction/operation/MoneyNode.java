package net.stardust.base.model.economy.transaction.operation;

import net.stardust.base.model.economy.transaction.Transaction;

public class MoneyNode implements Operation {

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var price = transaction.getValue();
        var buyerMoney = transaction.getNegotiators().getBuyer().getWallet().getMoney(price.getCurrency());
        if(!buyerMoney.isSubtractionPossible(price.getValue())) {
            throw OperationFailedException.fromKey("buyer-money");
        }
    }
    
}
