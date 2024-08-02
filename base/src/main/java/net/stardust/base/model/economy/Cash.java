package net.stardust.base.model.economy;

import net.stardust.base.model.economy.wallet.Wallet;

/**
 * Represents an entity that can perform transactions
 */
@FunctionalInterface
public interface Cash {
    
    /**
     * Returns the wallet of this entity.
     * @return the wallet of this entity.
     */
    Wallet getWallet();

}
