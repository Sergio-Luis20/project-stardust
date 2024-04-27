package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.ItemHolder;

public interface ItemNegotiators extends Negotiators {
    
    ItemHolder getBuyer();
    ItemHolder getSeller();

    @Override
    default ItemNegotiators reverse() {
        return from(getSeller(), getBuyer());
    }
    
    static ItemNegotiators from(ItemHolder buyer, ItemHolder seller) {
        return new ItemNegotiators() {
            
            @Override
            public ItemHolder getBuyer() {
                return buyer;
            }
    
            @Override
            public ItemHolder getSeller() {
                return seller;
            }
    
        };
    }

}
