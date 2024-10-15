package net.stardust.base.sequence.room;

import java.util.Objects;

import net.stardust.base.model.economy.villager.VillagerShop;

public class TradeRoom extends Room {

    private VillagerShop shop;
    
    public TradeRoom(VillagerShop shop) {
        super();
        this.shop = Objects.requireNonNull(shop, "shop");
    }

    public VillagerShop getShop() {
        return shop;
    }

}
