package net.stardust.base.model.economy.shop;

import net.stardust.base.model.economy.Cash;
import net.stardust.base.model.economy.owner.ServerShopOwner;
import net.stardust.base.model.economy.owner.ShopOwner;
import net.stardust.base.model.economy.wallet.ServerWallet;
import net.stardust.base.model.economy.wallet.Wallet;

public interface ServerShop extends Shop, Cash {
    
    @Override
    default Wallet getWallet() {
        return ServerWallet.INSTANCE;
    }

    @Override
    default ShopOwner getOwner() {
        return ServerShopOwner.INSTANCE;
    }

}
