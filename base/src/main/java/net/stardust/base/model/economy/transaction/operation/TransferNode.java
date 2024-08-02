package net.stardust.base.model.economy.transaction.operation;

import java.util.ArrayList;
import java.util.List;

import net.stardust.base.model.economy.transaction.ItemNegotiators;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;

public class TransferNode implements Operation {

    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        var money = transaction.getValue();
        var currency = money.getCurrency();
        var value = money.getValue();

        var pair = transaction.getNegotiators();
        var buyer = pair.getBuyer();
        var seller = pair.getSeller();

        var buyerWallet = buyer.getWallet();
        var sellerWallet = seller.getWallet();
        buyerWallet.getMoney(currency).subtract(value);
        sellerWallet.getMoney(currency).add(value);

        var crud = new PlayerWalletCrud();
        List<PlayerWallet> list = new ArrayList<>();
        if (buyerWallet instanceof PlayerWallet playerWallet) {
            list.add(playerWallet);
        }
        if (sellerWallet instanceof PlayerWallet playerWallet) {
            list.add(playerWallet);
        }
        if (!list.isEmpty()) {
            if (!crud.updateAll(list)) {
                throw OperationFailedException.fromKey("could-not-transfer", getClass());
            }
        }

        if (transaction instanceof ItemTransaction itemTransaction) {
            var itemPair = (ItemNegotiators) pair;
            var buyerStorage = itemPair.getBuyer().getStorage();
            var sellerStorage = itemPair.getSeller().getStorage();

            itemTransaction.getItems().forEach(item -> {
                buyerStorage.addItem(item);
                sellerStorage.removeItem(item);
            });
        }
    }

}
