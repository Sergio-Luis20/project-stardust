package net.stardust.base.utils.gameplay;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.inventory.MerchantRecipe;

import net.stardust.base.Stardust;

public class VillagerBuilder {
    
    private Location location;
    private Profession profession;
    private Type type;
    private boolean baby;
    private boolean ai;
    private List<MerchantRecipe> trades;

    public VillagerBuilder() {
        profession = Profession.NONE;
        type = Type.PLAINS;
        ai = true;
        trades = Collections.emptyList();
    }

    public VillagerBuilder location(Location location) {
        this.location = location;
        return this;
    }

    public VillagerBuilder profession(Profession profession) {
        this.profession = profession;
        return this;
    }

    public VillagerBuilder type(Type type) {
        this.type = type;
        return this;
    }

    public VillagerBuilder baby(boolean baby) {
        this.baby = baby;
        return this;
    }

    public VillagerBuilder ai(boolean ai) {
        this.ai = ai;
        return this;
    }

    public VillagerBuilder trades(List<MerchantRecipe> trades) {
        this.trades = trades;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public Profession getProfession() {
        return profession;
    }

    public Type getType() {
        return type;
    }

    public boolean isBaby() {
        return baby;
    }

    public boolean hasAI() {
        return ai;
    }

    public List<MerchantRecipe> getTrades() {
        return trades;
    }

    public Villager build() {
        Stardust.notNull("No villager property is allowed to be null", location, profession, type, trades);
        if (location.getWorld() == null) {
            throw new NullPointerException("location.world is null");
        }
        for (MerchantRecipe trade : trades) {
            if (trade == null) {
                throw new NullPointerException("trades list must not contain null values");
            }
        }

        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setProfession(profession);
        villager.setVillagerType(type);
        villager.setAI(ai);
        if (baby) {
            villager.setBaby();
        } else {
            villager.setAdult();
        }
        villager.setRecipes(trades);
        return villager;
    }

}
