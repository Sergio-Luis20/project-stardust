package net.stardust.base.model.economy.transaction.operation;

import java.util.ArrayList;
import java.util.List;

import net.stardust.base.model.economy.Cash;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.transaction.ItemNegotiators;
import net.stardust.base.model.economy.transaction.ItemTransaction;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.model.economy.wallet.PlayerWallet;
import net.stardust.base.model.economy.wallet.Wallet;
import net.stardust.base.utils.database.crud.PlayerWalletCrud;

/**
 * <p>This operation transfers the money being negotiated
 * in a {@link Transaction} from the buyer to the seller.
 * Also, if it is an {@link ItemTransaction}, the items
 * are also transfered from the seller {@link Storage} to
 * the buyer one.</p>
 * 
 * <p>This operation only do transferencies, it doesn't check
 * for validation or legality of money or items, that is,
 * it doesn't check if the buyer has enought money to pay
 * the seller or if the seller has items to sell to the buyer
 * etc... Use other {@link Operation} nodes to do that.</p>
 * 
 * <p>If the {@link Wallet} object returned by the negotiators
 * ({@link Cash#getWallet()}) is an instance of {@link PlayerWallet},
 * then a {@link PlayerWalletCrud} will be created to update wallets
 * in database. This is done directly, and because of that, it blocks
 * until the result is available; keep that in mind.</p>
 * 
 * @see Transaction
 * @see ItemTransaction
 * @see ItemNegotiators
 * @see Storage
 * @see Operation
 * @see Wallet
 * @see PlayerWallet
 * @see PlayerWalletCrud
 * @see Cash
 * @see Cash#getWallet()
 * 
 * @author Sergio Luis
 */
public class TransferNode implements Operation {

    /**
     * Executes money transferencies between buyer and seller
     * wallets. If the {@link Transaction} object is an implementation
     * of {@link ItemTransaction}, the items are also transfered from
     * seller {@link Storage} to buyer one. Check {@link TransferNode}
     * documentation for more important details.
     * 
     * @see TransferNode
     * @param transaction the transaction for execute transferencies
     * @throws OperationFailedException if buyer or seller wallets are instances
     * of {@link PlayerWallet} and {@link PlayerWalletCrud#updateAll(List)} returns
     * false, meaning that it could not update the wallets in database
     */
    @Override
    public void execute(Transaction transaction) throws OperationFailedException {
        // Transfer money from buyer to seller
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

        // Update wallets in database in case of some being a PlayerWallet
        var crud = new PlayerWalletCrud();
        List<PlayerWallet> list = new ArrayList<>();
        if (buyerWallet instanceof PlayerWallet playerWallet) {
            list.add(playerWallet);
        }
        if (sellerWallet instanceof PlayerWallet playerWallet) {
            list.add(playerWallet);
        }
        if (!list.isEmpty() && !crud.updateAll(list)) {
            throw OperationFailedException.fromKey("could-not-transfer", this);
        }

        // Transfer items from seller to buyer in case of an ItemTransaction
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
