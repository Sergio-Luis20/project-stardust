package net.stardust.base.model.economy.villager;

import org.bukkit.Location;
import org.bukkit.entity.Villager;

import net.stardust.base.utils.gameplay.VillagerBuilder;

public class VillagerShop {
    
    private Villager villager;
    private Location location;

    public VillagerShop(VillagerBuilder builder) {
        villager = builder.build();
        location = builder.getLocation();
    }

    public Villager getVillager() {
        return villager;
    }

    public Location getLocation() {
        return location;
    }

}
