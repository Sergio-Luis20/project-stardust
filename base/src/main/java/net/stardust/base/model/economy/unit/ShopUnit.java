package net.stardust.base.model.economy.unit;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.storage.Storage;

/**
 * A unit of a shop. This represents mostly a section
 * of a shop, like a chest with its respective sign in
 * a case of shop made of that. Can be a single villager
 * in a shop made of villagers, etc...
 * 
 * @author Sergio Luis
 */
public interface ShopUnit {

    /**
     * Returns the storage of this unit. A unit can belong to various
     * ShopUnits and even to various Shops if and only if the ShopOwner
     * of those shops are the same of the Shop object of thiis ShopUnit.
     * @return the storage of this unit.
     */
    Storage getStorage();

    /**
     * The item being negotiated
     * @return the item.
     */
    ItemStack getItem();

}
