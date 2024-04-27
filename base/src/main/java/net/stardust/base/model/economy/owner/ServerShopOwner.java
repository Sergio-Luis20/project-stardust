package net.stardust.base.model.economy.owner;

import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import net.stardust.base.model.economy.shop.ServerShop;
import net.stardust.base.model.economy.shop.Shop;

public final class ServerShopOwner implements ShopOwner {

    public static final ServerShopOwner INSTANCE = new ServerShopOwner();

    private Set<ServerShop> shops;

    private ServerShopOwner() {
        Set<ServerShop> shops = new HashSet<>();
        var loader = ServiceLoader.load(ServerShop.class);
        loader.forEach(shops::add);
        this.shops = Collections.unmodifiableSet(shops);
    }

    @Override
    public Set<? extends Shop> getShops() {
        return shops;
    }
    
}
