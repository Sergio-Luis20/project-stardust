package net.stardust.base.model.economy.transaction;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import net.stardust.base.model.economy.MonetaryEntity;

public interface ItemTransaction extends Transaction {

    /**
     * Returns the list of items being sold. In
     * case of symbolic transactions link payments,
     * an empty list must be returned, never null.
     * @return the items being sold.
     */
    List<ItemStack> getItems();

    /**
     * Returns the pair buyer-seller which, in this case,
     * are instances of ItemHolder.
     */
    ItemNegotiators getNegotiators();

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
