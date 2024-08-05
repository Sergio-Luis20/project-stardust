package net.stardust.base.model.economy.transaction;

import net.stardust.base.model.economy.ItemHolder;

/**
 * Interface that can supply entities of an {@link ItemTransaction}.
 * 
 * @see ItemTransaction
 * @see ItemHolder
 * 
 * @author Sergio Luis
 */
public interface ItemNegotiators extends Negotiators {
    
    /**
     * Returns the buyer of this transaction as {@link ItemHolder}.
     * 
     * @see ItemHolder
     * @see #getSeller()
     * @return the buyer
     */
    ItemHolder getBuyer();

    /**
     * Returns the seller of this transaction as {@link ItemHolder}.
     * 
     * @see ItemHolder
     * @see #getBuyer()
     * @return the seller
     */
    ItemHolder getSeller();

    /**
     * Creates a {@link ItemNegotiators} whose buyer is the seller
     * of this object and whose seller is the buyer of this object.
     * 
     * @see Negotiators
     * @see Negotiators#reverse()
     * @see ItemNegotiators
     * @see ItemHolder
     * @return the reverse buyer-seller relation {@link ItemNegotiators} object
     */
    @Override
    default ItemNegotiators reverse() {
        return from(getSeller(), getBuyer());
    }
    
    /**
     * Creates a new {@link ItemNegotiators} from a buyer and a seller
     * as {@link ItemHolder}.
     * 
     * @see ItemNegotiators
     * @see ItemHolder
     * @param buyer the buyer
     * @param seller the seller
     * @return the created {@link ItemNegotiators} object
     */
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
