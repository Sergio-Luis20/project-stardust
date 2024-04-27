package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.storage.ServerStorage;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.wallet.ServerWallet;
import net.stardust.base.model.economy.wallet.Wallet;

public class ServerItemHolder implements ItemHolder {

    public static final ServerItemHolder INSTANCE = new ServerItemHolder();

    private ServerItemHolder() {}

    @Override
    public Wallet getWallet() {
        return ServerWallet.INSTANCE;
    }

    @Override
    public Storage getStorage() {
        return ServerStorage.INSTANCE;
    }
    
}
