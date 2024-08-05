package net.stardust.base.model.economy;

import net.stardust.base.model.economy.transaction.Negotiators;
import net.stardust.base.model.economy.transaction.Transaction;
import net.stardust.base.model.economy.wallet.Wallet;

/**
 * Represents an entity that can perform transactions.
 * 
 * @see Transaction
 * @see Negotiators
 * 
 * @author Sergio Luis
 */
@FunctionalInterface
public interface Cash {
    
    /**
     * Returns the wallet of this entity.
     * 
     * @see Wallet
     * @return the wallet of this entity.
     */
    Wallet getWallet();

}
