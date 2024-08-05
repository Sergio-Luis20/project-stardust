package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.Cash;

/**
 * Interface that can supply entities of a {@link Transaction}: buyer and seller.
 * 
 * @see Transaction
 * @see Cash
 * 
 * @author Sergio Luis
 */
public interface Negotiators {
    
    /**
     * Returns the buyer of a transaction.
     * 
     * @see Transaction
     * @see Cash
     * @return the buyer
     */
    Cash getBuyer();

    /**
     * Returns the seller of a transaction.
     * 
     * @see Transaction
     * @see Cash
     * @return the seller
     */
    Cash getSeller();

    /**
     * Creates a {@link Negotiators} whose buyer is the seller
     * of this object and whose seller is the buyer of this object.
     * 
     * @see Transaction
     * @see Cash
     * @see Negotiators
     * @return the reverse buyer-seller relation {@link Negotiators} object
     */
    default Negotiators reverse() {
        return from(getSeller(), getBuyer());
    }
    
    /**
     * Static factory for creating a simple {@link Negotiators}.
     * 
     * @see Transaction
     * @see Cash
     * @param buyer the buyer
     * @param seller the seller
     * @return the {@link Negotiators} instance
     */
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
