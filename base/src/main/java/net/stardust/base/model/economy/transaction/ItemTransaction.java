package net.stardust.base.model.economy.transaction;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.ItemHolder;
import net.stardust.base.model.economy.MonetaryEntity;

/**
 * Interface that represents a transaction between negotiators
 * and consists in trading {@link ItemStack}s too, not only money.
 * 
 * @see ItemStack
 * @see Transaction
 * @see ItemNegotiators
 * 
 * @author Sergio Luis
 */
public interface ItemTransaction extends Transaction {

    /**
     * Returns the list of items being sold. In
     * case of symbolic transactions link payments,
     * an empty list must be returned, never null.
     * Also, the list of items must not contain null
     * elements.
     * 
     * @see ItemStack
     * @return the items being sold.
     */
    List<ItemStack> getItems();

    /**
     * Returns the pair buyer-seller which, in this case,
     * are instances of {@link ItemHolder}.
     * 
     * @see ItemNegotiators
     * @see ItemHolder
     * @return the negotiators of this transaction
     */
    ItemNegotiators getNegotiators();

    /**
     * Creates a new {@link ItemTransaction} using the specified parameters.
     * 
     * @see Negotiators
     * @see MonetaryEntity
     * @see ItemNegotiators
     * @see #getItems()
     * @see #getNegotiators()
     * @param value the value being negotiated (normally the price of the items)
     * @param items the list of items being negotiated
     * @param negotiators the negotiators
     * @return the new {@link ItemTransaction} instance
     */
    static ItemTransaction newItemTransaction(MonetaryEntity value, List<ItemStack> items, ItemNegotiators negotiators) {
        return new ItemTransaction() {

            @Override
            public MonetaryEntity getValue() {
                return value;
            }
            
            @Override
            public List<ItemStack> getItems() {
                return items;
            }

            @Override
            public ItemNegotiators getNegotiators() {
                return negotiators;
            }

        };
    }
    
}
