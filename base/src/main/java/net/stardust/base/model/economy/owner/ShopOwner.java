package net.stardust.base.model.economy.owner;

import java.util.Set;

import net.stardust.base.model.economy.shop.Shop;

public interface ShopOwner {
    
    Set<? extends Shop> getShops();

}
