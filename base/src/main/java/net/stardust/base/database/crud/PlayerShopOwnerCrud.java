package net.stardust.base.database.crud;

import java.util.UUID;

import net.stardust.base.model.economy.owner.PlayerShopOwner;

public class PlayerShopOwnerCrud extends Crud<UUID, PlayerShopOwner> {
    
    public PlayerShopOwnerCrud() {
        super(PlayerShopOwner.class);
    }

}
