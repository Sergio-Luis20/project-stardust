package net.stardust.base.model.economy.sign;

import java.io.Serializable;

import net.stardust.base.model.economy.owner.ShopOwner;
import net.stardust.base.model.economy.owner.ShopOwnerProvider;
import net.stardust.base.model.economy.storage.Storage;
import net.stardust.base.model.economy.unit.ShopUnit;
import net.stardust.base.model.economy.unit.SimpleShopUnit;

public record SignUnitData(SignShopData shopData, Storage storage) implements Serializable {
    
    public ShopUnit toShopUnit() {
        return new SimpleShopUnit(storage, shopData.getItem());
    }

    public ShopOwner getShopOwner() {
        return ShopOwnerProvider.getShopOwner(shopData.getIdentifier());
    }

}
