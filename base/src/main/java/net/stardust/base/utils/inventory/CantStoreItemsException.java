package net.stardust.base.utils.inventory;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@Getter
public class CantStoreItemsException extends Exception {

    private Inventory inventory;
    private Player player;

    public CantStoreItemsException(Inventory inventory) {
        this.inventory = inventory;
    }

    public CantStoreItemsException(Inventory inventory, Player player) {
        this.inventory = inventory;
        this.player = player;
    }

}
