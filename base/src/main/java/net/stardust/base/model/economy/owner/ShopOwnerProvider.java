package net.stardust.base.model.economy.owner;

import net.stardust.base.ServerIdentifier;
import net.stardust.base.database.crud.PlayerShopOwnerCrud;
import net.stardust.base.model.Identifier;
import net.stardust.base.model.IllegalIdentifierException;
import net.stardust.base.model.user.PlayerIdentifier;

public final class ShopOwnerProvider {
    
    private ShopOwnerProvider() {}

    public static ShopOwner getShopOwner(Identifier<?> identifier) {
        if(identifier == null) {
            throw new NullPointerException("identifier");
        }
        if(identifier instanceof ServerIdentifier) {
            return ServerShopOwner.INSTANCE;
        } else if(identifier instanceof PlayerIdentifier playerIdentifier) {
            PlayerShopOwnerCrud crud = new PlayerShopOwnerCrud();
            PlayerShopOwner shopOwner = crud.getOrNull(playerIdentifier.getId());
            if(shopOwner == null) {
                throw new IllegalIdentifierException(playerIdentifier);
            }
            return shopOwner;
        } else {
            throw new IllegalIdentifierException(identifier);
        }
    }

}
