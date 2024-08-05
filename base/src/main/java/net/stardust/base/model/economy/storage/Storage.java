package net.stardust.base.model.economy.storage;

import java.util.List;

import org.bukkit.inventory.ItemStack;

/**
 * This class represents a storage of {@link ItemStack}s. It can be
 * a chest in case of a sign in its wall, a villager, some
 * inventory or whatever other storing type. This includes external
 * methods of storing, like files or databases.
 * 
 * @see ItemStack
 * 
 * @author Sergio Luis
 */
public interface Storage {

    /**
     * Returns true if the operation of adding the item into the
     * storage was successful, false otherwise.
     * 
     * @param item the item being inserted into the storage
     * @return true if the item was successfully inserted, false otherwise
    */
    boolean addItem(ItemStack item);

    /**
     * Returns true if the operation of removing the item from the
     * storage was successful, false otherwise. If {@link #hasItem(ItemStack)}
     * returns false for this exactly item being passed as argument in this
     * method, it should return false.
     * 
     * @param item the item being removed from the storage
     * @return true if the item was successfully removed, false otherwise
     */
    boolean removeItem(ItemStack item);

    /**
     * Returns true if this storage has a copy of this item inside it,
     * false otherwise. The equality concept is arbitrary, it can be
     * just {@code item.equals(other)} or whatever other comparing strategy.
     * 
     * @param item the item being verified if it is inside this storage
     * @return true if t his storage has a copy of this item inside it, false
     * otherwise
     */
    boolean hasItem(ItemStack item);

    /**
     * Returns if this storage can store all the items in the list.
     * 
     * @param items the items to check if can be stored
     * @return true if the items can be stored, false otherwise
     */
    boolean canStore(List<ItemStack> items);

}
