package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.storage.ServerStorage;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.wallet.ServerWallet;
import net.stardust.base.model.economy.wallet.Wallet;

/**
 * The server itself as {@link ItemHolder}, meaning that it can
 * perform {@link ItemTransaction}s. This is util from when for
 * example a player is buying or selling to the server, which
 * has infinity money to buy and infinity items to sell, as it
 * generates both.
 * 
 * @see ItemHolder
 * @see ItemTransaction
 * 
 * @author Sergio Luis
 */
public class ServerItemHolder implements ItemHolder {

    /**
     * Singleton instance for {@link ServerItemHolder}.
     */
    public static final ServerItemHolder INSTANCE = new ServerItemHolder();

    private ServerItemHolder() {
    }

    /**
     * Returns the server wallet ({@link ServerWallet#INSTANCE}).
     * 
     * @see ServerWallet
     * @see ServerWallet#INSTANCE
     * @return the server wallet
     */
    @Override
    public Wallet getWallet() {
        return ServerWallet.INSTANCE;
    }

    /**
     * Returns the server storage ({@link ServerStorage#INSTANCE}).
     * 
     * @see ServerStorage
     * @see ServerStorage#INSTANCE
     * @return the server storage
     */
    @Override
    public Storage getStorage() {
        return ServerStorage.INSTANCE;
    }
    
}
