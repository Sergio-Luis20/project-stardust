package net.stardust.base.model.economy.shop;

import net.stardust.base.model.economy.owner.ShopOwner;

/**
 * Interface that represents a Shop of any kind.
 * Can be a player shop with chests and signs, a villager,
 * server shops, npcs inside dungeons etc...
 * 
 * @author Sergio Luis
 */
public interface Shop {
    
    /**
     * Returns the owner of this Shop. Each Shop
     * has only 1 ShopOwner, but one ShopOwner can have many Shops.
     * It's a relation many-for-one.
     * @return the owner of this shop.
     */
    ShopOwner getOwner();

    /**
     * The name of this shop. Shops that belongs to the same ShopOwner
     * cannot have the same name.
     * @return the name of this Shop.
     */
    String getName();

}
