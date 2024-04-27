package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.Cash;

public interface Negotiators {
    
    Cash getBuyer();
    Cash getSeller();

    default Negotiators reverse() {
        return from(getSeller(), getBuyer());
    }
    
    static Negotiators from(Cash buyer, Cash seller) {
        return new Negotiators() {
            
            @Override
            public Cash getBuyer() {
                return buyer;
            }
    
            @Override
            public Cash getSeller() {
                return seller;
            }
    
        };
    }

}
